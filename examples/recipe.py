import json

def run(spark, input_message, antennas, messages_df):
    message = input_message.collect()[0]
    antenna_id = message['antenna_id']
    temperature_str = message['payload']

    results_final_json = {
        'results': temperature_str
    }

    print('Result for antenna with id ' + antenna_id + ' is: ' + temperature_str)

    return results_final_json
