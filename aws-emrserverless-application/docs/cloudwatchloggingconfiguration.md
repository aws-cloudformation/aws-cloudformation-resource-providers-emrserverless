# AWS::EMRServerless::Application CloudWatchLoggingConfiguration

## Syntax

To declare this entity in your AWS CloudFormation template, use the following syntax:

### JSON

<pre>
{
    "<a href="#enabled" title="Enabled">Enabled</a>" : <i>Boolean</i>,
    "<a href="#loggroupname" title="LogGroupName">LogGroupName</a>" : <i>String</i>,
    "<a href="#logstreamnameprefix" title="LogStreamNamePrefix">LogStreamNamePrefix</a>" : <i>String</i>,
    "<a href="#encryptionkeyarn" title="EncryptionKeyArn">EncryptionKeyArn</a>" : <i>String</i>,
    "<a href="#logtypemap" title="LogTypeMap">LogTypeMap</a>" : <i>[ <a href="logtypemapkeyvaluepair.md">LogTypeMapKeyValuePair</a>, ... ]</i>
}
</pre>

### YAML

<pre>
<a href="#enabled" title="Enabled">Enabled</a>: <i>Boolean</i>
<a href="#loggroupname" title="LogGroupName">LogGroupName</a>: <i>String</i>
<a href="#logstreamnameprefix" title="LogStreamNamePrefix">LogStreamNamePrefix</a>: <i>String</i>
<a href="#encryptionkeyarn" title="EncryptionKeyArn">EncryptionKeyArn</a>: <i>String</i>
<a href="#logtypemap" title="LogTypeMap">LogTypeMap</a>: <i>
      - <a href="logtypemapkeyvaluepair.md">LogTypeMapKeyValuePair</a></i>
</pre>

## Properties

#### Enabled

If set to false, CloudWatch logging will be turned off. Defaults to false.

_Required_: No

_Type_: Boolean

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### LogGroupName

_Required_: No

_Type_: String

_Minimum Length_: <code>1</code>

_Maximum Length_: <code>512</code>

_Pattern_: <code>^[\.\-_/#A-Za-z0-9]+$</code>

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### LogStreamNamePrefix

_Required_: No

_Type_: String

_Minimum Length_: <code>1</code>

_Maximum Length_: <code>512</code>

_Pattern_: <code>^[^:*]*$</code>

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### EncryptionKeyArn

_Required_: No

_Type_: String

_Minimum Length_: <code>20</code>

_Maximum Length_: <code>2048</code>

_Pattern_: <code>^arn:(aws[a-zA-Z0-9-]*):kms:[a-zA-Z0-9\-]*:(\d{12})?:key\/[a-zA-Z0-9-]+$</code>

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### LogTypeMap

The specific log-streams which need to be uploaded to CloudWatch.

_Required_: No

_Type_: List of <a href="logtypemapkeyvaluepair.md">LogTypeMapKeyValuePair</a>

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

