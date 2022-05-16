# AWS::EMRServerless::Application WorkerConfiguration

## Syntax

To declare this entity in your AWS CloudFormation template, use the following syntax:

### JSON

<pre>
{
    "<a href="#cpu" title="Cpu">Cpu</a>" : <i>String</i>,
    "<a href="#memory" title="Memory">Memory</a>" : <i>String</i>,
    "<a href="#disk" title="Disk">Disk</a>" : <i>String</i>
}
</pre>

### YAML

<pre>
<a href="#cpu" title="Cpu">Cpu</a>: <i>String</i>
<a href="#memory" title="Memory">Memory</a>: <i>String</i>
<a href="#disk" title="Disk">Disk</a>: <i>String</i>
</pre>

## Properties

#### Cpu

Per worker CPU resource. vCPU is the only supported unit and specifying vCPU is optional.

_Required_: Yes

_Type_: String

_Minimum_: <code>1</code>

_Maximum_: <code>15</code>

_Pattern_: <code>^[1-9][0-9]*(\s)?(vCPU|vcpu|VCPU)?$</code>

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### Memory

Per worker memory resource. GB is the only supported unit and specifying GB is optional.

_Required_: Yes

_Type_: String

_Minimum_: <code>1</code>

_Maximum_: <code>15</code>

_Pattern_: <code>^[1-9][0-9]*(\s)?(GB|gb|gB|Gb)?$</code>

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### Disk

Per worker Disk resource. GB is the only supported unit and specifying GB is optional

_Required_: No

_Type_: String

_Minimum_: <code>1</code>

_Maximum_: <code>15</code>

_Pattern_: <code>^[1-9][0-9]*(\s)?(GB|gb|gB|Gb)$</code>

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)
