import sys, psycopg2, requests, json, time
from datetime import datetime
from kafka import KafkaProducer

DIMENSION_TABLES_QUERY = '''\
    (SELECT * FROM {}) {}'''

KPI_DB_QUERY = '''\
    INSERT INTO {} (timestamp, result{})\
    VALUES ('{}', '{}'::json{})'''

class TokenProvider(object):
    def __init__(self, client_id, client_secret, token_url):
        self.client_id = client_id
        self.client_secret = client_secret
        self.token_url = token_url
        self.token_req_payload = {'grant_type': 'client_credentials'}
    def token(self):
        token_response = requests.post(self.token_url,
                                       data=self.token_req_payload, verify=False, allow_redirects=False,
                                       auth=(self.client_id, self.client_secret))
        if token_response.status_code != 200:
            print("Failed to obtain token from the OAuth 2.0 server", file=sys.stderr)
            sys.exit(1)
        print("TokenProvider: Successfully obtained a new token!")
        tokens = json.loads(token_response.text)
        token = tokens['access_token']
        # print(token)
        return token


def fetch_from_eventlog_one(spark, namespace, message_id, message_columns):
    '''Fetches messages from the EventLog.

    TODO: documentation.

    :param message_id:

    '''
    columns = message_columns.replace(" ", "").replace('[','').replace(']','').split(',')

    catalog = """
    {
        "table": {
            "namespace": \""""+namespace+"""\",
            "name": "Events"
        },
        "rowkey": "key",
        "columns": {
            "message_id":{"cf":"rowkey", "col":"key", "type":"string"},"""
    for column in columns:
        catalog+="""
            \""""+column+"""\":{"cf":"messages", "col":\""""+column+"""\", "type":"string"},"""
    catalog=catalog[0:-1]
    catalog+="""
        }
    }"""

    messages = spark.read.format("org.apache.spark.sql.execution.datasources.hbase").options(catalog=catalog).load()
    return messages.filter(messages["message_id"] == message_id).drop(messages["message_type"]).drop(messages["message_id"]).drop(messages["slug"])

def fetch_from_eventlog(spark, namespace, message_type, message_columns):
    '''Fetches messages from the EventLog.

    TODO: documentation.

    :param message_type:

    '''
    columns = message_columns.replace(" ", "").replace('[','').replace(']','').split(',')

    catalog = """
    {
        "table": {
            "namespace": \""""+namespace+"""\",
            "name": "Events"
        },
        "rowkey": "key",
        "columns": {
            "message_id":{"cf":"rowkey", "col":"key", "type":"string"},"""
    for column in columns:
        catalog+="""
            \""""+column+"""\":{"cf":"messages", "col":\""""+column+"""\", "type":"string"},"""
    catalog=catalog[0:-1]
    catalog+="""
        }
    }"""

    messages = spark.read.format("org.apache.spark.sql.execution.datasources.hbase").options(catalog=catalog).load()
    return messages.filter(messages["message_type"] == message_type).drop(messages["message_type"]).drop(messages["message_id"]).drop(messages["slug"])

def fetch_from_master_data(spark, dimension_tables_url, username, password, table):
    '''Fetches master data from a Dimension table.

    TODO: documentation.

    :param message_id:

    '''
    query = DIMENSION_TABLES_QUERY.format(table,table)

    return spark.read.jdbc(
        url=dimension_tables_url,
        properties={'user':username,'password':password,'driver': 'org.postgresql.Driver'},
        table=query)

def save_result_to_kpidb(kpidb_host, kpidb_port, kpidb_name, username, password, kpi_table, message, message_columns, result):
    '''Connects to KPI DB and stores the `results_list`.

    TODO: documentation.

    :param result:

    '''

    if result is None:
        return

    KPI_DB_SETTINGS = {
        'dbname': kpidb_name,
        'host': kpidb_host,
        'port': kpidb_port,
        'user': username,
        'password': password,
    }

    columns_str = ""
    if message_columns != "":
        columns = message_columns.replace(" ", "").replace('[','').replace(']','').split(',')
        columns.remove("payload")
        columns.remove("message_type")
        columns.remove("slug")
        columns_str = ','+','.join(columns)


    fields = []
    fields_str = ""
    if message != '':
        message_data = message.collect()[0]
        for column in columns:
            fields.append(message_data[column])
        fields_str = ",'"+"','".join(fields)+"'"

    query = KPI_DB_QUERY.format(
        kpi_table,
        columns_str,
        datetime.now(),
        json.dumps(result),
        fields_str
    )

    try:
        connection = psycopg2.connect(**KPI_DB_SETTINGS)
        cursor = connection.cursor()
        cursor.execute(query)
        connection.commit()
    except Exception:
        print('Unable to connect to the database.')
        raise
    finally:
        cursor.close()
        connection.close()
    print("Result saved in KPI db")

def publish_result(kafka_broker_address, security, client_id, client_secret, token_url, ssl_cafile, ssl_certfile, ssl_keyfile, slug, message_type, result):

    producer = KafkaProducer(
        bootstrap_servers=[kafka_broker_address],
        security_protocol=security,
        sasl_mechanism='OAUTHBEARER',
        sasl_oauth_token_provider=TokenProvider(client_id, client_secret, token_url),
        ssl_check_hostname=False,
        ssl_cafile=ssl_cafile,
        ssl_certfile=ssl_certfile,
        ssl_keyfile=ssl_keyfile,
        value_serializer=lambda m: json.dumps(m).encode('ascii')
    )

    print('producer-kafka: Sending message...')
    future = producer.send(message_type, "{'message_type':'"+message_type+"', 'slug':'"+slug+"', 'payload':"+str(result)+"}")

    # Block for 'synchronous' sends
    try:
        record_metadata = future.get(timeout=10)
    except Exception as e:
        logging.error(e)
        sys.exit(1)
        pass

    # Successful result returns assigned partition and offset
    print('record_metadata.topic: ', record_metadata.topic)
    print('record_metadata.partition', record_metadata.partition)
    print('record_metadata.offset', record_metadata.offset)

