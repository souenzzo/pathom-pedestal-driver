(ns pathom-pedestal-driver.core-test
  (:require [clojure.test :refer [deftest is are testing]]
            [io.pedestal.http :as http]
            [pathom-pedestal-driver.core :as pedestal-driver]
            [com.wsscode.pathom.core :as p]
            [com.wsscode.pathom.diplomat.http :as p.http]
            [com.wsscode.pathom.connect :as pc]
            [clojure.edn :as edn]))

(defn echo
  [req]
  {:headers {"Ok" "!!"}
   :body    (-> req
                (dissoc :servlet-response :servlet :url-for :servlet-request :context-path)
                (update :body slurp))
   :status  200})

(def routes
  `#{["/echo/*args" :any [echo]]})



(def service-fn
  (-> {::http/routes routes
       ::http/type   :jetty
       ::http/port   8080
       ::http/join?  false}
      http/default-interceptors
      http/dev-interceptors
      http/create-server
      ::http/service-fn))

(defmulti resolver-fn pc/resolver-dispatch)
(def indexes (atom {}))
(def defresolver (pc/resolver-factory resolver-fn indexes))

(defresolver `echo-test
             {::pc/params [:simple?]
              ::pc/output [::response]}
             (fn [env _]
               (let [opts (if (get-in env [:ast :params :simple?])
                            {::p.http/url "/echo/hello"}
                            {::p.http/url          "/echo/hello?query=string#frag"
                             ::p.http/method       ::p.http/post
                             ::p.http/body         "{body}"
                             ::p.http/content-type ::p.http/json
                             ::p.http/accept       "raw-type"
                             ::p.http/headers      {"auth" "123"}})]
                 {::response (-> env
                                 (merge opts)
                                 p.http/request
                                 (update :body edn/read-string))})))

(def parser
  (p/parser {::p/plugins [(p/env-plugin {::p/reader             [p/map-reader pc/all-readers]
                                         ::pc/resolver-dispatch resolver-fn
                                         ::p.http/driver        (pedestal-driver/request-factory service-fn)
                                         ::pc/indexes           @indexes})]}))



(deftest integration
  (testing "simple test"
    (is (= (::response (parser {} '[(::response {:simple? true})]))
           {:body    {:async-supported?   true
                      :body               ""
                      :character-encoding "UTF-8"
                      :content-length     0
                      :content-type       ""
                      :headers            {"content-length" "0"
                                           "content-type"   ""
                                           "origin"         ""}
                      :path-info          "/echo/hello"
                      :path-params        {:args "hello"}
                      :protocol           "HTTP/1.1"
                      :query-string       nil
                      :remote-addr        "127.0.0.1"
                      :request-method     :get
                      :scheme             nil
                      :server-name        nil
                      :server-port        -1
                      :uri                "/echo/hello"}
            :headers {"Content-Security-Policy"           "object-src 'none'; script-src 'unsafe-inline' 'unsafe-eval' 'strict-dynamic' https: http:;"
                      "Content-Type"                      "application/edn"
                      "Ok"                                "!!"
                      "Strict-Transport-Security"         "max-age=31536000; includeSubdomains"
                      "X-Content-Type-Options"            "nosniff"
                      "X-Download-Options"                "noopen"
                      "X-Frame-Options"                   "DENY"
                      "X-Permitted-Cross-Domain-Policies" "none"
                      "X-XSS-Protection"                  "1; mode=block"}
            :status  200})))
  (testing "full test"
    (is (= (::response (parser {} '[(::response {:simple? false})]))
           {:body    {:async-supported?   true
                      :body               "{body}"
                      :character-encoding "UTF-8"
                      :content-length     0
                      :content-type       "application/json"
                      :headers            {"accept"         "raw-type"
                                           "auth"           "123"
                                           "content-length" "0"
                                           "content-type"   "application/json"
                                           "origin"         ""}
                      :params             {:query "string#frag"}
                      :path-info          "/echo/hello"
                      :path-params        {:args "hello"}
                      :protocol           "HTTP/1.1"
                      :query-params       {:query "string#frag"}
                      :query-string       "query=string#frag"
                      :remote-addr        "127.0.0.1"
                      :request-method     :post
                      :scheme             nil
                      :server-name        nil
                      :server-port        -1
                      :uri                "/echo/hello"}
            :headers {"Content-Security-Policy"           "object-src 'none'; script-src 'unsafe-inline' 'unsafe-eval' 'strict-dynamic' https: http:;"
                      "Content-Type"                      "application/edn"
                      "Ok"                                "!!"
                      "Strict-Transport-Security"         "max-age=31536000; includeSubdomains"
                      "X-Content-Type-Options"            "nosniff"
                      "X-Download-Options"                "noopen"
                      "X-Frame-Options"                   "DENY"
                      "X-Permitted-Cross-Domain-Policies" "none"
                      "X-XSS-Protection"                  "1; mode=block"}
            :status  200}))))
