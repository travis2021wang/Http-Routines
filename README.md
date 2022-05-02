# Http-Routines
The Best OKHttp Routines!

```
GET("") {
            Header("", "")
            Header("", "")
        }
            .onError { i, headers -> res = ""}
            .onSuccess<String> { i, headers, t -> res = t.orEmpty() }
```
