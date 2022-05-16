# AWS::EMRServerless::Application InitialCapacityConfig

## Syntax

To declare this entity in your AWS CloudFormation template, use the following syntax:

### JSON

<pre>
{
    "<a href="#workercount" title="WorkerCount">WorkerCount</a>" : <i>Integer</i>,
    "<a href="#workerconfiguration" title="WorkerConfiguration">WorkerConfiguration</a>" : <i><a href="workerconfiguration.md">WorkerConfiguration</a></i>
}
</pre>

### YAML

<pre>
<a href="#workercount" title="WorkerCount">WorkerCount</a>: <i>Integer</i>
<a href="#workerconfiguration" title="WorkerConfiguration">WorkerConfiguration</a>: <i><a href="workerconfiguration.md">WorkerConfiguration</a></i>
</pre>

## Properties

#### WorkerCount

Initial count of workers to be initialized when an Application is started. This count will be continued to be maintained until the Application is stopped

_Required_: Yes

_Type_: Integer

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### WorkerConfiguration

_Required_: Yes

_Type_: <a href="workerconfiguration.md">WorkerConfiguration</a>

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)
