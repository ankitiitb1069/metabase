(ns metabase.query-processor.async-test
  (:require [clojure.core.async :as a]
            [expectations :refer [expect]]
            [metabase.query-processor :as qp]
            [metabase.query-processor.async :as qp.async]
            [metabase.test.data :as data]
            [metabase.test.util.async :as tu.async]))

;; running a query async should give you the same results as running that query synchronously
(let [query {:database (data/id)
             :type     :query
             :query    {:source-table (data/id :venues)
                        :fields       [[:field-id (data/id :venues :name)]]
                        :limit        5}}]
  (expect
    (qp/process-query query)
    (tu.async/with-open-channels [result-chan (qp.async/process-query query)]
      (first (a/alts!! [result-chan (a/timeout 1000)])))))

(expect
  [{:name         "NAME"
    :display_name "Name"
    :base_type    :type/Text
    :special_type :type/Name
    :fingerprint  {:global {:distinct-count 100, :nil% 0.0},
                   :type   #:type {:Text
                                   {:percent-json   0.0,
                                    :percent-url    0.0,
                                    :percent-email  0.0,
                                    :average-length 15.63}}}}]
  (tu.async/with-open-channels [result-chan (qp.async/result-metadata-for-query-async
                                             {:database (data/id)
                                              :type     :query
                                              :query    {:source-table (data/id :venues)
                                                         :fields       [[:field-id (data/id :venues :name)]]}})]
    (first (a/alts!! [result-chan (a/timeout 1000)]))))
