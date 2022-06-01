# AWS::EMRServerless::Application AutoStopConfiguration

Configuration for Auto Stop of Application

## Syntax

To declare this entity in your AWS CloudFormation template, use the following syntax:

### JSON

<pre>
{
    "<a href="#enabled" title="Enabled">Enabled</a>" : <i>Boolean</i>,
    "<a href="#idletimeoutminutes" title="IdleTimeoutMinutes">IdleTimeoutMinutes</a>" : <i>Integer</i>
}
</pre>

### YAML

<pre>
<a href="#enabled" title="Enabled">Enabled</a>: <i>Boolean</i>
<a href="#idletimeoutminutes" title="IdleTimeoutMinutes">IdleTimeoutMinutes</a>: <i>Integer</i>
</pre>

## Properties

#### Enabled

If set to true, the Application will automatically stop after being idle. Defaults to true.

_Required_: No

_Type_: Boolean

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### IdleTimeoutMinutes

The amount of time [in minutes] to wait before auto stopping the Application when idle. Defaults to 15 minutes.

_Required_: No

_Type_: Integer

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)
