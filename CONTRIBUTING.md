# FlareBot Contribution Guide
Arsen and Walshy are very picky about the code quality for FlareBot so it is *very* important you adhere to this guide.

### Big Dos and Don'ts
 - Don't PR single line corrections
 - Always test your code the best you can
 - Don't change code just to reorganise or to restructure without making sure it has a worthwhile purpose
 - Do use separate branches for features requiring more than 10 commits **This will be enforced on a per-case basis**

**We very highly recommend that you use IntelliJ for contributions as we will not accept any files or gitignores concerning Eclipse. *We do not provide any help for people using Eclipse!***
### Formatting
 We mostly use IntelliJ's default formatter with a few changes:
 - The minimum gap between the class header and body is 1 line
 - There are to be no breaks larger than 3 lines in your contribution
 - A line break must occur after a newline escape character in strings. IntelliJ should handle this if you keep your cursor within the quotation marks after the escape character and press `Enter`

### Code Practices
A lot of the changes requested in PRs is related to memory usage and efficiency. There are a few things that are enforced in FlareBot's codebase.
 - Single use variables are not allowed. They must be inline
 - Switch statements are not used for consistency
 - Code efficiency is vital when dealing with arrays and data. Always make sure to use `getOrDefault` or `computeIfAbsent` where necessary
 - Checking argument lengths in commands that have sub commands must happen inside the code block of the sum command for fluidity and maximum understanding
 - Creating new objects should be avoided where available
 - Variables have to be used 3 times or more. If they are not then they need to be inlined *This doesn't apply to variables which are final or effectively final (Single item array for interfacing with lambas for example)*
 - Big sets of commits such as data restructures must be put in their own branch.

### Usage Formatting
###### If you don't follow these instructions when creating a new command it will automatically be denied
The command usage now has the following format:
 - The command (not the description) needs to be enclosed in graves \`.
 - `[optional_argument]` for arguments that can be included but are not required.
 - `<required_argument>` for arguments that are required.
 - `subcommand` sub commands **must not** be enclosed in anything.
 - `command|command1|command2` multiple sub commands not enclosed by anything and separated by a pipe.
 - Spaces are only permitted between arguments.
 - `` `{%}command` `` is the format for commands
