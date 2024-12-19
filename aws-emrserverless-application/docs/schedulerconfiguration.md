# AWS::EMRServerless::Application SchedulerConfiguration
 
 The scheduler configuration for batch and streaming jobs running on this application. Supported with release labels emr-7.0.0 and above.
 
 ## Syntax
 
 To declare this entity in your AWS CloudFormation template, use the following syntax:
 
 ### JSON
 
 <pre>
 {
     "<a href="#queuetimeoutminutes" title="QueueTimeoutMinutes">QueueTimeoutMinutes</a>" : <i>Integer</i>,
     "<a href="#maxconcurrentruns" title="MaxConcurrentRuns">MaxConcurrentRuns</a>" : <i>Integer</i>
 }
 </pre>
 
 ### YAML
 
 <pre>
 <a href="#queuetimeoutminutes" title="QueueTimeoutMinutes">QueueTimeoutMinutes</a>: <i>Integer</i>
 <a href="#maxconcurrentruns" title="MaxConcurrentRuns">MaxConcurrentRuns</a>: <i>Integer</i>
 </pre>
 
 ## Properties
 
 #### QueueTimeoutMinutes
 
 The maximum duration in minutes for the job in QUEUED state. If scheduler configuration is enabled on your application, the default value is 360 minutes (6 hours). The valid range is from 15 to 720.
 
 _Required_: No
 
 _Type_: Integer
 
 _Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)
 
 #### MaxConcurrentRuns
 
 The maximum concurrent job runs on this application. If scheduler configuration is enabled on your application, the default value is 15. The valid range is 1 to 1000.
 
 _Required_: No
 
 _Type_: Integer
 
 _Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)