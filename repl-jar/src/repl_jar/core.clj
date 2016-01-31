(ns repl-jar.core
  (:gen-class
   :name repl.ApplicationREPL
   :main true
   :methods [^:static  [start [] void]])
  (:require 
   [ring.adapter.jetty :refer [run-jetty]]
   [compojure.core :as compojure :refer [GET defroutes]]
   [compojure.handler :as handler]
   [ring.middleware.session :as session]
   [ring.middleware.stacktrace :as stacktrace]
   [ring.middleware.params :as params]
   [clojure.stacktrace :as stacktrace-clj]
   [hiccup.page :refer [include-css include-js html5]]
   [ring.middleware.resource :as resource]
   [clojure.data.json :as json]))

(defonce repl-sessions 
  (ref {}))

(defn current-bindings 
  "When creating a new enviroment, start with the current environment (with caveats)"
  []
  (binding [*ns* *ns* 
            *warn-on-reflection* *warn-on-reflection*
            *math-context* *math-context*
            *print-meta* *print-meta*
            *print-length* *print-length*
            *print-level* *print-level*
            *compile-path* (System/getProperty "clojure.compile.path" "classes")
            *command-line-args* *command-line-args*
            *assert* *assert*
            *1 nil
            *2 nil
            *3 nil
            *e nil]
    (get-thread-bindings)))

(defn bindings-for 
  "Given a session key, pull back the corresponding environment"
  [session-key]
  (when-not (@repl-sessions session-key)
    (dosync
     (commute repl-sessions assoc session-key (current-bindings))))
  (@repl-sessions session-key))

(defn store-bindings-for 
  "Given a new session key, add it to the map of environments"
  [session-key]
  (dosync
   (commute repl-sessions assoc session-key (current-bindings))))

(defmacro with-session 
  "Given a session identifier, pull up the corresponding environment"
  [session-key & body]
  `(with-bindings (bindings-for ~session-key)
     (let [r# ~@body]
       (store-bindings-for ~session-key)
       r#)))

(defn do-eval 
  "Evaluate the expression against an environment identified by the session"
  [txt session-key]
  (with-session session-key
    (let [form (binding [*read-eval* false] (read-string txt))]
      (with-open [writer (java.io.StringWriter.)]
        (binding [*out* writer]
          (try
            (let [r (pr-str (eval form))]
              (str (.toString writer) (str r)))
            (catch Exception e (str (stacktrace-clj/root-cause e)))))))))

(defn handle-request 
  "Given an expression from the REPL and a request, get the session and evaluate the expresion"
  [msg req] 
  (let [session-key ((:headers req) "sec-websocket-key")
        result (do-eval msg session-key)]
    result))

(defn html-page 
  "Generate the main page using the hiccup library"
  []
  (html5
   [:head
    [:meta {:name "viewport" :content "width=device-width, initial-scale=1"}]
    (include-css "/css/webrepl.css")
    (include-js "/js/jquery-2.1.4.min.js"
                "/js/jquery.console.js"
                "/js/webrepl.js")
    [:link {:rel "icon" :href "data:;base64,iVBORw0KGgo="}]
    [:title "Application REPL"]]
   [:body
    [:div#console]]))

(defroutes app-routes
  "map the http actions and paths to functions"
  (GET "/"  [] (html-page))
  (GET "/repl" req 
    (handle-request ((:query-params req) ":line-key") req)))

(def ring-app
  "Wrap the routes with handlers for parameters, css, sessions and error handling" 
  (-> app-routes
      (params/wrap-params)
      (resource/wrap-resource "public") 
      (session/wrap-session)
      (stacktrace/wrap-stacktrace)))

(defn start-jetty 
  "Pass the routes and the port to Jetty"
  []
  (run-jetty ring-app {:port 8090 :join? false}))

(defn -main
  "The main method and Clojure equivalent of public static void main(String[] args)"
  [& args]
  (start-jetty))

(defn -start  
  "External function to be called as a static Java method"
  [] 
  (-main))

