(defproject pathom-pedestal-driver "0.1.0"
  :description "Use io.pedestal.test/response-for as a com.wsscode.pathom.diplomat.http/driver"
  :url "https://github.com/souenzzo/pathom-pedestal-driver"
  :license {:name "Eclipse Public License"
            :url  "http://www.eclipse.org/legal/epl-v10.html"}
  :profiles {:dev {:dependencies [[org.clojure/test.check "0.10.0-alpha3"]
                                  [io.pedestal/pedestal.jetty "0.5.3"]
                                  [org.slf4j/slf4j-nop "1.8.0-beta2"]]}}
  :dependencies [[org.clojure/clojure "1.9.0" :scope "provided"]
                 [com.wsscode/pathom "2.1.3" :scope "provided"]
                 [io.pedestal/pedestal.service "0.5.3" :scope "provided"]])
