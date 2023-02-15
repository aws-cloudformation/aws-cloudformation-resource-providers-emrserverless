# AWS::EMRServerless::Application ImageConfigurationInput

The image configuration.

## Syntax

To declare this entity in your AWS CloudFormation template, use the following syntax:

### JSON

<pre>
{
    "<a href="#imageuri" title="ImageUri">ImageUri</a>" : <i>String</i>
}
</pre>

### YAML

<pre>
<a href="#imageuri" title="ImageUri">ImageUri</a>: <i>String</i>
</pre>

## Properties

#### ImageUri

The URI of an image in the Amazon ECR registry. This field is required when you create a new application. If you leave this field blank in an update, Amazon EMR will remove the image configuration.

_Required_: No

_Type_: String

_Minimum_: <code>1</code>

_Maximum_: <code>1024</code>

_Pattern_: <code>^([a-z0-9]+[a-z0-9-.]*)\/((?:[a-z0-9]+(?:[._-][a-z0-9]+)*\/)*[a-z0-9]+(?:[._-][a-z0-9]+)*)(?:\:([a-zA-Z0-9_][a-zA-Z0-9-._]{0,299})|@(sha256:[0-9a-f]{64}))$</code>

_Update requires_: [Some Interruptions](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#updates-with-some-interruption)