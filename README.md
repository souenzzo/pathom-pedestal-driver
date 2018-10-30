# pathom-pedestal-driver

Use io.pedestal.test/response-for as a com.wsscode.pathom.diplomat.http/driver

## Installation

lein/boot:
```clojure
[pathom-pedestal-driver "0.1.0"]
```

tools-deps:
```clojure
pathom-pedestal-driver {:mvn/version "0.1.0"}
```

## Usage

```
  (:require ...
            [pathom-pedestal-driver.core :as pedestal-driver])


(def service-fn
  (-> {::http/routes routes
       ::http/type   :jetty
       ::http/port   8080
       ::http/join?  false}
      http/default-interceptors
      http/dev-interceptors
      http/create-server
      ::http/service-fn))

(def parser
  (p/parser {::p/plugins [(p/env-plugin {::p.http/driver (pedestal-driver/request-factory service-fn)
                                         ...})]}))

```

## License

Copyright © 2018 FIXME

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
