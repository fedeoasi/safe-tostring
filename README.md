# SafeToString

Use scalameta's macro annotations to generate a toString that obfuscates
values marked as `@hidden`.

All you need to do is annotate your case class as `@safeToString` and
annotate the fields you want to hide:
```scala
@safeToString case class Credentials(username: String, @hidden password: String)
```

A sample `Credentials` object will be printed as `Credentials(user,****)`.
