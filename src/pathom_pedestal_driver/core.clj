(ns pathom-pedestal-driver.core
  (:require [com.wsscode.pathom.diplomat.http :as http]
            [io.pedestal.test :as pedestal.test]
            [clojure.spec.alpha :as s]))

(defn encode-type->header
  [encode-type]
  (cond
    (string? encode-type)
    encode-type

    (keyword? encode-type)
    (str "application/" (name encode-type))))

;; TODO: ::http/form-params, ::http/debug? ::http/as
(defn build-request-args
  [service-fn {::http/keys [url method content-type accept body headers] :as req}]
  (let [q? (partial contains? req)
        method (cond
                 (string? method) (keyword method)
                 (qualified-keyword? method) (keyword (name method))
                 :else :get)
        headers (cond-> headers
                        (q? ::http/content-type) (assoc "Content-Type" (encode-type->header content-type))
                        (q? ::http/accept) (assoc "Accept" (encode-type->header accept)))]
    (cond-> [service-fn method url :headers headers]
            (q? ::http/body) (into [:body body]))))

(defn request-factory
  [service-fn]
  (fn [req]
    (s/assert ::http/request req)
    (->> req
         (build-request-args service-fn)
         (apply pedestal.test/response-for))))
