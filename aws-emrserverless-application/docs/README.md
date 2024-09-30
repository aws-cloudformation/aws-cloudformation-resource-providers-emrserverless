# AWS::EMRServerless::Application

Resource schema for AWS::EMRServerless::Application Type

## Syntax

To declare this entity in your AWS CloudFormation template, use the following syntax:

### JSON

<pre>
{
    "Type" : "AWS::EMRServerless::Application",
    "Properties" : {
        "<a href="#architecture" title="Architecture">Architecture</a>" : <i>String</i>,
        "<a href="#name" title="Name">Name</a>" : <i>String</i>,
        "<a href="#releaselabel" title="ReleaseLabel">ReleaseLabel</a>" : <i>String</i>,
        "<a href="#type" title="Type">Type</a>" : <i>String</i>,
        "<a href="#initialcapacity" title="InitialCapacity">InitialCapacity</a>" : <i>[ <a href="initialcapacityconfigkeyvaluepair.md">InitialCapacityConfigKeyValuePair</a>, ... ]</i>,
        "<a href="#maximumcapacity" title="MaximumCapacity">MaximumCapacity</a>" : <i><a href="maximumallowedresources.md">MaximumAllowedResources</a></i>,
        "<a href="#tags" title="Tags">Tags</a>" : <i>[ <a href="tag.md">Tag</a>, ... ]</i>,
        "<a href="#autostartconfiguration" title="AutoStartConfiguration">AutoStartConfiguration</a>" : <i><a href="autostartconfiguration.md">AutoStartConfiguration</a></i>,
        "<a href="#autostopconfiguration" title="AutoStopConfiguration">AutoStopConfiguration</a>" : <i><a href="autostopconfiguration.md">AutoStopConfiguration</a></i>,
        "<a href="#imageconfiguration" title="ImageConfiguration">ImageConfiguration</a>" : <i><a href="imageconfigurationinput.md">ImageConfigurationInput</a></i>,
        "<a href="#monitoringconfiguration" title="MonitoringConfiguration">MonitoringConfiguration</a>" : <i><a href="monitoringconfiguration.md">MonitoringConfiguration</a></i>,
        "<a href="#runtimeconfiguration" title="RuntimeConfiguration">RuntimeConfiguration</a>" : <i>[ <a href="configurationobject.md">ConfigurationObject</a>, ... ]</i>,
        "<a href="#interactiveconfiguration" title="InteractiveConfiguration">InteractiveConfiguration</a>" : <i><a href="interactiveconfiguration.md">InteractiveConfiguration</a></i>,
        "<a href="#networkconfiguration" title="NetworkConfiguration">NetworkConfiguration</a>" : <i><a href="networkconfiguration.md">NetworkConfiguration</a></i>,
        "<a href="#workertypespecifications" title="WorkerTypeSpecifications">WorkerTypeSpecifications</a>" : <i><a href="workertypespecifications.md">WorkerTypeSpecifications</a></i>,
        "<a href="#schedulerconfiguration" title="SchedulerConfiguration">SchedulerConfiguration</a>" : <i><a href="schedulerconfiguration.md">SchedulerConfiguration</a></i>
    }
}
</pre>

### YAML

<pre>
Type: AWS::EMRServerless::Application
Properties:
    <a href="#architecture" title="Architecture">Architecture</a>: <i>String</i>
    <a href="#name" title="Name">Name</a>: <i>String</i>
    <a href="#releaselabel" title="ReleaseLabel">ReleaseLabel</a>: <i>String</i>
    <a href="#type" title="Type">Type</a>: <i>String</i>
    <a href="#initialcapacity" title="InitialCapacity">InitialCapacity</a>: <i>
      - <a href="initialcapacityconfigkeyvaluepair.md">InitialCapacityConfigKeyValuePair</a></i>
    <a href="#maximumcapacity" title="MaximumCapacity">MaximumCapacity</a>: <i><a href="maximumallowedresources.md">MaximumAllowedResources</a></i>
    <a href="#tags" title="Tags">Tags</a>: <i>
      - <a href="tag.md">Tag</a></i>
    <a href="#autostartconfiguration" title="AutoStartConfiguration">AutoStartConfiguration</a>: <i><a href="autostartconfiguration.md">AutoStartConfiguration</a></i>
    <a href="#autostopconfiguration" title="AutoStopConfiguration">AutoStopConfiguration</a>: <i><a href="autostopconfiguration.md">AutoStopConfiguration</a></i>
    <a href="#imageconfiguration" title="ImageConfiguration">ImageConfiguration</a>: <i><a href="imageconfigurationinput.md">ImageConfigurationInput</a></i>
    <a href="#monitoringconfiguration" title="MonitoringConfiguration">MonitoringConfiguration</a>: <i><a href="monitoringconfiguration.md">MonitoringConfiguration</a></i>
    <a href="#runtimeconfiguration" title="RuntimeConfiguration">RuntimeConfiguration</a>: <i>
      - <a href="configurationobject.md">ConfigurationObject</a></i>
    <a href="#interactiveconfiguration" title="InteractiveConfiguration">InteractiveConfiguration</a>: <i><a href="interactiveconfiguration.md">InteractiveConfiguration</a></i>
    <a href="#networkconfiguration" title="NetworkConfiguration">NetworkConfiguration</a>: <i><a href="networkconfiguration.md">NetworkConfiguration</a></i>
    <a href="#workertypespecifications" title="WorkerTypeSpecifications">WorkerTypeSpecifications</a>: <i><a href="workertypespecifications.md">WorkerTypeSpecifications</a></i>
    <a href="#schedulerconfiguration" title="SchedulerConfiguration">SchedulerConfiguration</a>: <i><a href="schedulerconfiguration.md">SchedulerConfiguration</a></i>
</pre>

## Properties

#### Architecture

The cpu architecture of an application.

_Required_: No

_Type_: String

_Allowed Values_: <code>ARM64</code> | <code>X86_64</code>

_Update requires_: [Some interruptions](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-some-interrupt)

#### Name

User friendly Application name.

_Required_: No

_Type_: String

_Minimum Length_: <code>1</code>

_Maximum Length_: <code>64</code>

_Pattern_: <code>^[A-Za-z0-9._\/#-]+$</code>

_Update requires_: [Replacement](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-replacement)

#### ReleaseLabel

EMR release label.

_Required_: Yes

_Type_: String

_Minimum Length_: <code>1</code>

_Maximum Length_: <code>64</code>

_Pattern_: <code>^[A-Za-z0-9._/-]+$</code>

_Update requires_: [Some interruptions](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-some-interrupt)

#### Type

The type of the application

_Required_: Yes

_Type_: String

_Update requires_: [Replacement](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-replacement)

#### InitialCapacity

_Required_: No

_Type_: List of <a href="initialcapacityconfigkeyvaluepair.md">InitialCapacityConfigKeyValuePair</a>

_Update requires_: [Some interruptions](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-some-interrupt)

#### MaximumCapacity

_Required_: No

_Type_: <a href="maximumallowedresources.md">MaximumAllowedResources</a>

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### Tags

Tag map with key and value

_Required_: No

_Type_: List of <a href="tag.md">Tag</a>

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### AutoStartConfiguration

Configuration for Auto Start of Application

_Required_: No

_Type_: <a href="autostartconfiguration.md">AutoStartConfiguration</a>

_Update requires_: [Some interruptions](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-some-interrupt)

#### AutoStopConfiguration

Configuration for Auto Stop of Application

_Required_: No

_Type_: <a href="autostopconfiguration.md">AutoStopConfiguration</a>

_Update requires_: [Some interruptions](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-some-interrupt)

#### ImageConfiguration

The image configuration.

_Required_: No

_Type_: <a href="imageconfigurationinput.md">ImageConfigurationInput</a>

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### MonitoringConfiguration

Monitoring configuration for batch and interactive JobRun.

_Required_: No

_Type_: <a href="monitoringconfiguration.md">MonitoringConfiguration</a>

_Update requires_: [Some interruptions](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-some-interrupt)

#### RuntimeConfiguration

Runtime configuration for batch and interactive JobRun.

_Required_: No

_Type_: List of <a href="configurationobject.md">ConfigurationObject</a>

_Update requires_: [Some interruptions](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-some-interrupt)

#### InteractiveConfiguration

_Required_: No

_Type_: <a href="interactiveconfiguration.md">InteractiveConfiguration</a>

_Update requires_: [Some interruptions](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-some-interrupt)

#### NetworkConfiguration

_Required_: No

_Type_: <a href="networkconfiguration.md">NetworkConfiguration</a>

_Update requires_: [Some interruptions](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-some-interrupt)

#### WorkerTypeSpecifications

_Required_: No

_Type_: <a href="workertypespecifications.md">WorkerTypeSpecifications</a>

_Update requires_: [Some interruptions](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-some-interrupt)

 #### SchedulerConfiguration
  
 The scheduler configuration for batch and streaming jobs running on this application. Supported with release labels emr-7.0.0 and above.
  
 _Required_: No
  
 _Type_: <a href="schedulerconfiguration.md">SchedulerConfiguration</a>
  
 _Update requires_: [Some interruptions](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-some-interrupt)
 
## Return Values

### Ref

When you pass the logical ID of this resource to the intrinsic `Ref` function, Ref returns the ApplicationId.

### Fn::GetAtt

The `Fn::GetAtt` intrinsic function returns a value for a specified attribute of this type. The following are the available attributes and sample return values.

For more information about using the `Fn::GetAtt` intrinsic function, see [Fn::GetAtt](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/intrinsic-function-reference-getatt.html).

#### Arn

The Amazon Resource Name (ARN) of the EMR Serverless Application.

#### ApplicationId

The ID of the EMR Serverless Application.

