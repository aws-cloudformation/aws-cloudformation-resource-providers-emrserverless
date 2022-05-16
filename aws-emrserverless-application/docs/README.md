# AWS::EMRServerless::Application

Resource schema for AWS::EMRServerless::Application Type

## Syntax

To declare this entity in your AWS CloudFormation template, use the following syntax:

### JSON

<pre>
{
    "Type" : "AWS::EMRServerless::Application",
    "Properties" : {
        "<a href="#name" title="Name">Name</a>" : <i>String</i>,
        "<a href="#releaselabel" title="ReleaseLabel">ReleaseLabel</a>" : <i>String</i>,
        "<a href="#type" title="Type">Type</a>" : <i>String</i>,
        "<a href="#initialcapacity" title="InitialCapacity">InitialCapacity</a>" : <i>[ <a href="initialcapacityconfigkeyvaluepair.md">InitialCapacityConfigKeyValuePair</a>, ... ]</i>,
        "<a href="#maximumcapacity" title="MaximumCapacity">MaximumCapacity</a>" : <i><a href="maximumallowedresources.md">MaximumAllowedResources</a></i>,
        "<a href="#tags" title="Tags">Tags</a>" : <i>[ <a href="tag.md">Tag</a>, ... ]</i>,
        "<a href="#autostartconfiguration" title="AutoStartConfiguration">AutoStartConfiguration</a>" : <i><a href="autostartconfiguration.md">AutoStartConfiguration</a></i>,
        "<a href="#autostopconfiguration" title="AutoStopConfiguration">AutoStopConfiguration</a>" : <i><a href="autostopconfiguration.md">AutoStopConfiguration</a></i>,
        "<a href="#networkconfiguration" title="NetworkConfiguration">NetworkConfiguration</a>" : <i><a href="networkconfiguration.md">NetworkConfiguration</a></i>,
    }
}
</pre>

### YAML

<pre>
Type: AWS::EMRServerless::Application
Properties:
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
    <a href="#networkconfiguration" title="NetworkConfiguration">NetworkConfiguration</a>: <i><a href="networkconfiguration.md">NetworkConfiguration</a></i>
</pre>

## Properties

#### Name

User friendly Application name.

_Required_: No

_Type_: String

_Minimum_: <code>1</code>

_Maximum_: <code>64</code>

_Pattern_: <code>^[A-Za-z0-9._\/#-]+$</code>

_Update requires_: [Replacement](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-replacement)

#### ReleaseLabel

EMR release label.

_Required_: Yes

_Type_: String

_Minimum_: <code>1</code>

_Maximum_: <code>64</code>

_Pattern_: <code>^[A-Za-z0-9._/-]+$</code>

_Update requires_: [Replacement](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-replacement)

#### Type

The type of the application

_Required_: Yes

_Type_: String

_Update requires_: [Replacement](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-replacement)

#### InitialCapacity

_Required_: No

_Type_: List of <a href="initialcapacityconfigkeyvaluepair.md">InitialCapacityConfigKeyValuePair</a>

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

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

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### AutoStopConfiguration

Configuration for Auto Stop of Application

_Required_: No

_Type_: <a href="autostopconfiguration.md">AutoStopConfiguration</a>

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### NetworkConfiguration

_Required_: No

_Type_: <a href="networkconfiguration.md">NetworkConfiguration</a>

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

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
