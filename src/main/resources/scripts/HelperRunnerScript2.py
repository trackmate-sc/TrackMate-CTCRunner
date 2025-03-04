#@File json_file( label="Path to a JSON file that defines the task." )

import json
from pprint import pprint

from ij import IJ
from fiji.plugin.trackmate import Logger
from fiji.plugin.trackmate.helper import TrackingMetricsType
from fiji.plugin.trackmate.helper.spt import SPTTrackingMetricsType
from fiji.plugin.trackmate.helper.ctc import CTCTrackingMetricsType
from fiji.plugin.trackmate.helper import HelperRunner
from fiji.plugin.trackmate.helper.HelperRunner import Builder
from fiji.plugin.trackmate.util import CircularLogFile



#---------------------------------
def run_helper(metrics, ground_truth_path, source_image_path, save_folder, helper_task_definition_path, log_file, spt_max_linking_distance, target_channel ):
	
	imp = IJ.openImage( source_image_path )
	if imp is None:
		print('Error. Could not open image file: ' + source_image_path)
		return
	
	units = "image units"
	if metrics == 'SPT':
		tracking_type = SPTTrackingMetricsType( spt_max_linking_distance, units )
	elif metrics == 'CTC':
		tracking_type = CTCTrackingMetricsType()
	else:
		print('Error. Uknown tracking metrics type: ' + metrics)
		return 
	
	logger = CircularLogFile( log_file, 100000 )
		
	builder = HelperRunner.create()
	runner = builder 								\
		.trackingMetricsType( tracking_type )	\
		.groundTruth( ground_truth_path )	\
		.savePath( save_folder )	\
		.runSettings( helper_task_definition_path )	\
		.image( imp )	\
		.batchLogger( logger )	\
		.trackmateLogger( logger )	\
		.targetChannel( target_channel )	\
		.get()
					
	if runner is None:
		print( 'Error. ' + builder.getErrorMessage() )
		return
	
	print('Starting parameter sweep')
	runner.run()
	print(runner.getCancelReason())
	print('Done')
#---------------------------------

def read_json_file(file_path):
	with open(file_path, 'r') as file:
	    data = json.load(file)
    	return data

#------------------------------------
def pretty_print_dict(obj, indent=0):
    def print_indented(s, indent_level):
        print(' ' * (2 * indent_level) + s)

    if isinstance(obj, dict):
        for key, value in obj.items():
            if isinstance(value, (dict, list, tuple)):
                print_indented(str(key) + ':', indent)
                pretty_print_dict(value, indent + 1)
            else:
                print_indented(str(key) + ': ' + str(value), indent)
    elif isinstance(obj, (list, tuple)):
        for item in obj:
            if isinstance(item, (dict, list, tuple)):
                pretty_print_dict(item, indent + 1)
            else:
                print_indented(str(item), indent)
    else:
        print_indented(str(obj), indent)
#---------------------------------
print(json_file)
json_dict = read_json_file( json_file.getAbsolutePath() )
if json_dict:
	print("Task file content:")
	pretty_print_dict(json_dict, indent=4)
	
	print('Starting helper')
	run_helper( json_dict['metrics'], \
		json_dict['ground_truth_path'], \ 
		json_dict['source_image_path'], \
		json_dict['save_folder'], \
		json_dict['helper_task_definition_path'], \
		json_dict['log_file'], \
		json_dict['spt_max_linking_distance'], \
		json_dict['target_channel'] )
	print('Done.')
	
