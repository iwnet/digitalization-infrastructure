import json

def run(spark, input_message, vessels, messages_df):
    message = input_message.collect()[0]
    vessel_id = message['vessel_id']
    ais_str = message['payload']

    results_final_json = {
        'results': ais_str
    }

    print('Result for vessel with id ' + vessel_id + ' is: ' + ais_str)

    return results_final_json
