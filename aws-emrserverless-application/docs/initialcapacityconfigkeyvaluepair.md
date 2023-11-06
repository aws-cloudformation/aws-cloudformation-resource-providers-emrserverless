# AWS::EMRServerless::Application InitialCapacityConfigKeyValuePair

## Syntax

To declare this entity in your AWS CloudFormation template, use the following syntax:

### JSON

<pre>
{
    "<a href="#key" title="Key">Key</a>" : <i>String</i>,
    "<a href="#value" title="Value">Value</a>" : <i><a href="initialcapacityconfig.md">InitialCapacityConfig</a></i>
}
</pre>

### YAML

<pre>
<a href="#key" title="Key">Key</a>: <i>String</i>
<a href="#value" title="Value">Value</a>: <i><a href="initialcapacityconfig.md">InitialCapacityConfig</a></i>
</pre>

## Properties

#### Key

Worker type for an analytics framework.

_Required_: Yes

_Type_: String

_Minimum Length_: <code>1</code>

_Maximum Length_: <code>50</code>

_Pattern_: <code>^[a-zA-Z]+[-_]*[a-zA-Z]+$</code>

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### Value

_Required_: Yes

_Type_: <a href="initialcapacityconfig.md">InitialCapacityConfig</a>

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

