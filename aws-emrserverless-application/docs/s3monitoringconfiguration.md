# AWS::EMRServerless::Application S3MonitoringConfiguration

## Syntax

To declare this entity in your AWS CloudFormation template, use the following syntax:

### JSON

<pre>
{
    "<a href="#loguri" title="LogUri">LogUri</a>" : <i>String</i>,
    "<a href="#encryptionkeyarn" title="EncryptionKeyArn">EncryptionKeyArn</a>" : <i>String</i>
}
</pre>

### YAML

<pre>
<a href="#loguri" title="LogUri">LogUri</a>: <i>String</i>
<a href="#encryptionkeyarn" title="EncryptionKeyArn">EncryptionKeyArn</a>: <i>String</i>
</pre>

## Properties

#### LogUri

_Required_: No

_Type_: String

_Minimum Length_: <code>1</code>

_Maximum Length_: <code>10280</code>

_Pattern_: <code>[\u0020-\uD7FF\uE000-\uFFFD\uD800\uDBFF-\uDC00\uDFFF\r\n\t]*</code>

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### EncryptionKeyArn

_Required_: No

_Type_: String

_Minimum Length_: <code>20</code>

_Maximum Length_: <code>2048</code>

_Pattern_: <code>^arn:(aws[a-zA-Z0-9-]*):kms:[a-zA-Z0-9\-]*:(\d{12})?:key\/[a-zA-Z0-9-]+$</code>

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

