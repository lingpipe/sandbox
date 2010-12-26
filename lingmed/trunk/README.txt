There is a fair amount of documentation in the docs directory. Run 'ant javadoc' to create
a docs/api folder with relevant javadoc. Also look at docs/overview.txt for detailed how to
on running tasks. Below is how to run a 'hello world' level implementation of LingBlast
with toy data contained in the distro. 

>ant shortLingBlast

You can see what it is doing by looking at the ant target in build.xml corresponding to 
shortLingBlast.

Suggested sequcence for exploration:

0) Copy log4j.properties.template to log4j.properties in lingmed/trunk/

1) Run 'ant javadoc' in the lingmed/trunk/ directory. It will generate the java doc and will
be in the folder /lingmed/trunk/docs/api.

2) Read the documentation in lingmed/trunk/read-me.html

3) Run shortLingBlast

4) There is also an lingmed/trunk/docs/overview.txt that overlaps with the getting_started.html 
documentation but has some unique information in it as well.




