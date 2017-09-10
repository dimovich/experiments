;; https://adambard.com/blog/buddy-password-auth-example/
;; https://blog.knoldus.com/2014/03/19/password-recovery-in-clojure/
;; http://grishaev.me/en/datomic-query?utm_source=dlvr.it&utm_medium=twitter
;; https://github.com/Day8/re-frame-http-fx
;; https://gist.github.com/Deraen/ef7f65d7ec26f048e2bb
;; http://cryto.net/~joepie91/blog/2016/06/13/stop-using-jwt-for-sessions/

;;
;; bin/run -m datomic.peer-server -h localhost -p 8998 -a admin,admin -d hello,datomic:mem://hello

;; use invitation code
;; (eliminates the need for activation mail)


(require
 '[clojure.core.async :refer [<!!]]
 '[datomic.client     :as    client]
 '[clojure.pprint     :refer [pprint]]
 '[experiments.db.schema])


(def db-state (atom {}))

(def user-schema [{:db/ident :user/username
                   :db/valueType :db.type/string
                   :db/cardinality :db.cardinality/one
                   :db/unique :db.unique/identity}

                  {:db/ident :user/password
                   :db/valueType :db.type/string
                   :db/cardinality :db.cardinality/one}

                  {:db/ident :user/email
                   :db/valueType :db.type/string
                   :db/cardinality :db.cardinality/one}])


(defn connect []
  (-> {:db-name "hello"
       :account-id client/PRO_ACCOUNT
       :secret "admin"
       :region "none"
       :endpoint "localhost:8998"
       :service "peer-server"
       :access-key "admin"}
      
      client/connect
      <!!))


(defn get-connection []
  (if-let [conn (:conn @db-state)]
    conn
    (if-let [conn (connect)]
      (swap! db-state assoc :conn conn)
      conn)))



(<!! (client/transact (get-connection) {:tx-data user-schema}))


(defn current-db []
  (client/db (get-connection)))


(defn random-uuid []
  (java.util.UUID/randomUUID))



(defn add-data [data]
  (let [data (if (vector? data) data [data])]
    (-> (get-connection)
        (client/transact {:tx-data data})
        <!!)))


(defn get-user [username]
  (let [db (current-db)
        conn (get-connection)]
    (->> {:query '[:find (pull ?e [*])
                   :in $ ?name
                   :where
                   [?e :user/username ?name]]
          :args [db name]}
         (client/q conn)
         <!!
         ffirst)))



(def user-data
  [{:user/name "dimovich"
    :user/password "password"
    :user/email "some@mail.com"}])


(add-data user-data)

(get-user "dimovich")

(defn add-user [{:keys [username password email]}]
  (-> [{:user/username username
        :user/password password
        :user/email email}]
      (add-data)))


(add-user {:name "radyon"
           :password "someotherpass"
           :email "radyon@gmail.com"})








;; convert keys of GET params to keywords
(defn wrap-get-params-to-key [handler]
  (fn [request]
    (-> (if (= :get (:request-method request))
          (-> request
              (update-in [:params]
                         #(reduce (fn [m [k v]]
                                    (assoc m (keyword k) v)) {} %)))
          request)
        
        handler)))
