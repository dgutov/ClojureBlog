(ns blog.system.utils
  "The namespace contains common functionality."
  (:use ring.util.response,
	compojure.core)
  (:require [clj-soy.template :as soy]
            [clojure.walk :as walk]
            [clojure.contrib.sql :as sql])
  (:import com.google.template.soy.data.SoyMapData))

(def ^{:doc "The name of the directory where the templates can be found"}
  *templates* "templates")

(defn ^{:doc "Just a syntax sugar for ring.util.response/redirect"}
  redirect-to [addr]
  (ring.util.response/redirect addr))

(defn ^{:doc "Builds and renders the soy-template.
	Parameters:
	  template-file -- template file to build
	  template-ns -- full template name including the namespace
	  params -- template parameters."}
  render [template-file template-ns params]
  (let [tpl (soy/build template-file)]
    (soy/render tpl
		template-ns
		(SoyMapData. (walk/stringify-keys params)))))

(defn ^{:doc "Renders an enclosing template (application template)
and includes the child template.
	Paremeters:
	  template-file -- child template file
	  template-ns -- full name of child template
	  params -- child template
	  app-template-file -- enclosing template file
	  app-template-ns -- full enclosing template name"}
  decorate-page [template-file template-ns params
		     app-template-file app-template-ns]
  (let [content (render template-file template-ns params)]
    (render app-template-file
	    app-template-ns
	    {:content content})))

(defn ^{:doc "Knows the exact paths to templates and their names.
Renders a correct template for each entity. Different entities
may have different enclosing templates, e.g. post has it's own
decorations and user -- it's own. So the structure of directories
for action \"new\" of the entity \"post\" should be
\"blog\\templates\\post\\new.soy\" and his enclosing template
should be located in \"blog\\templates\\post.soy\". The full name
of the enclosing template should be \"app.post\". The full name of
the child template should be \"post.new\""}
  render-rest-page [entity action params]
  (decorate-page (str *templates* "/" entity "/" action ".soy")
		 (str entity "." action)
		 params
		 (str *templates* "/app/" entity ".soy")
		 (str "app." entity)))

(defmacro ^{:doc "Parses the specified action and renders the corresponding
decorated template. The action should have format <entity>#<action>. E.g.:
post#new, comment#create, user#login. Usage example:
(render-action post#index {})"}
  render-action [action params]
  `(let [splitted# (.split ~(str action) "#")
	 entities# (first splitted#)
	 action# (second splitted#)]
     (render-rest-page entities# action# ~params)))

(defmacro ^{:doc "Just a syntax sugar for Compojure routing api.
(route GET \"/post/index\" post/index) will generate
(GET \"/post/index\" request (post/index request) "}
  route [method route function]
    `(~method ~route request# (~function request#)))

(defmacro ^{:doc "Automates parameters desctructuring for the user function.
Each user function defined in the routing table should accept just one argument --
request, which is a huge hash-map of request arguments.
Macro defaction destructures some of the parameters.
It produces two system parameters:
session -- the hash-map which contains the session values
params -- all the user parameters passed through the URL or through the posted form.
Additionally, the macro destructures all the user parameters specified in the
function argument list.
Usage: (defaction create [post_id] (println post_id))
Outcome: (defn create [{session :session, {post_id :post_id} :params, params :params}]
(println post_id))
Inside the generated function create there will be three local variables available:
session, params and post_id."}
  defaction [name params-vector & body]
  (let [params-hash (apply hash-map (flatten (map (fn [p] [p (keyword p)]) params-vector)))
	session (symbol "session")
	params (symbol "params")]
    `(defn ~name [{~params-hash :params, ~params :params,  ~session :session}]
       ~@body)))

(defn ^{:doc "Updates the session in the response.
	Parameters:
	  session -- updated session parameters map
	  response -- a Ring response map"}
  with-session [session response]
  (update-in response [:session]
             merge session))

(defmacro ^{:doc "Checks whether there is non-empty :user field in the session.
If there is such a field, then it evaluates the body. Otherwise it
redirects to the home page."}
  check-auth [& body]
  `(let [user# (:user ~'session)]
     (if (empty? user#)
       (redirect-to "/")
       (do ~@body))))

(defn ^{:doc "Parse the string as an int"}
  parse-int [s]
  (try (Integer/parseInt s)
       (catch NumberFormatException e 0)))

(defn ^{:doc "Database connection middleware"}
  wrap-db-access
  [handler db]
  (fn [request]
    (sql/with-connection db
      (handler request))))
