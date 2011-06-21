(ns blog.models.m-user
  (:use blog.system.utils)
  (:require [clojure.contrib.sql :as sql]))

(defn fetch [name password]
  (sql/with-query-results user
    ["SELECT * FROM blog_user where name = ? and password = ?" name password]
    (first user)))

