(ns blog.models.m-post
  (:use blog.system.db
	[blog.system.utils :rename {parse-int pint}])
  (:require [clojure.contrib.sql :as sql]))

(defn fetch-list []
  (sql/with-connection *db*
    (sql/with-query-results posts
      ["SELECT * FROM post order by id desc"]
      (or (doall posts) '()))))

(defn create [post]
  (sql/with-connection *db*
    (sql/insert-values "post" ["title" "body"]
		       [(:title post)
			(:text post)])))

(defn fetch [id]
  (sql/with-connection *db*
    (sql/with-query-results post
      ["SELECT * FROM post where id = ?" (pint id)]
      (first post))))

(defn update [post]
  (sql/with-connection *db*
    (sql/update-values "post" ["id = ?" (pint (:id post))]
                       (select-keys post [:title :body]))))

(defn delete [id]
  (sql/with-connection *db*
    (sql/delete-rows "post" ["id = ?" (pint id)])))
