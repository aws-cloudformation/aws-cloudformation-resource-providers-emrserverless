# AWS::EMRServerless::Application InteractiveConfiguration

## Syntax

To declare this entity in your AWS CloudFormation template, use the following syntax:

### JSON

<pre>
{
    "<a href="#livyendpointenabled" title="LivyEndpointEnabled">LivyEndpointEnabled</a>" : <i>Boolean</i>,
    "<a href="#studioenabled" title="StudioEnabled">StudioEnabled</a>" : <i>Boolean</i>
}
</pre>

### YAML

<pre>
<a href="#livyendpointenabled" title="LivyEndpointEnabled">LivyEndpointEnabled</a>: <i>Boolean</i>
<a href="#studioenabled" title="StudioEnabled">StudioEnabled</a>: <i>Boolean</i>
</pre>

## Properties

#### LivyEndpointEnabled

Enables an Apache Livy endpoint that you can connect to and run interactive jobs

_Required_: No

_Type_: Boolean

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### StudioEnabled

Enabled you to connect an Application to Amazon EMR Studio to run interactive workloads in a notebook

_Required_: No

_Type_: Boolean

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

