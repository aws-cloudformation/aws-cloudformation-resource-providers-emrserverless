# AWS::EMRServerless::Application ConfigurationObject

Configuration for a JobRun.

## Syntax

To declare this entity in your AWS CloudFormation template, use the following syntax:

### JSON

<pre>
{
    "<a href="#classification" title="Classification">Classification</a>" : <i>String</i>,
    "<a href="#properties" title="Properties">Properties</a>" : <i><a href="configurationobject-properties.md">Properties</a></i>,
    "<a href="#configurations" title="Configurations">Configurations</a>" : <i>[ <a href="configurationobject.md">ConfigurationObject</a>, ... ]</i>
}
</pre>

### YAML

<pre>
<a href="#classification" title="Classification">Classification</a>: <i>String</i>
<a href="#properties" title="Properties">Properties</a>: <i><a href="configurationobject-properties.md">Properties</a></i>
<a href="#configurations" title="Configurations">Configurations</a>: <i>
      - <a href="configurationobject.md">ConfigurationObject</a></i>
</pre>

## Properties

#### Classification

_Required_: Yes

_Type_: String

_Minimum Length_: <code>1</code>

_Maximum Length_: <code>1024</code>

_Pattern_: <code>.*\S.*</code>

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### Properties

_Required_: No

_Type_: <a href="configurationobject-properties.md">Properties</a>

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### Configurations

_Required_: No

_Type_: List of <a href="configurationobject.md">ConfigurationObject</a>

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

