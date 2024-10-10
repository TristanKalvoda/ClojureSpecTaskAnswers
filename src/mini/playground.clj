(ns mini.playground
  (:require [clojure.spec.alpha :as s])
  (:require [clojure.spec.gen.alpha :as gen])
  (:require [clojure.spec.test.alpha :as stest]))




;; Collections:
;; Write spec definitions that conform:

;; A set of keywords or strings, no more than 5 total.
(s/def :ex/keywords-or-strings (s/or :keywords (s/coll-of keyword? :max-count 5 :kind set?)
                                     :strings (s/coll-of string? :max-count 5 :kind set?)))

(s/valid? :ex/keywords-or-strings #{:a})
(s/valid? :ex/keywords-or-strings #{"a" "b" "c" "d" "e"})
(s/valid? :ex/keywords-or-strings '[:a :b :c :d :e])
(gen/generate (s/gen :ex/keywords-or-strings))

;; A vector of non-nil elements
(defn non-nil? [x]
  (if x true ; takes advantage of the fact that nil values and false values evaluate to false and everything else evaluates to true
      (false? x))) ; false itself is non-nil
(s/def :ex/vector-non-nil (s/coll-of non-nil? :kind vector?))

(s/valid? :ex/vector-non-nil nil)
(s/valid? :ex/vector-non-nil '[nil nil 1 2 3])
(s/valid? :ex/vector-non-nil '[1])
(s/valid? :ex/vector-non-nil '[1 2 3 :hi "hello"])
(s/valid? :ex/vector-non-nil '[1 2 3 :hi "hello" false])

;; A vector or a list (but not a set) of at least 3 elements, each of which is a non-negative even number.
;; (s/def :ex/non-small-vector-or-list (s/coll-of some? :kind (or list? vector?)))
;; (s/def :ex/non-small-vector-or-list (s/or some? :vector (s/coll-of :kind vector? :min-count 3)
;;                                           :list (s/coll-of :kind list? :min-count 3)))
(s/def :ex/non-small-vector-or-list-non-neg-even (s/coll-of (s/and pos-int? even?) :kind #(or (list? %) (vector? %)) :min-count 3))

(s/valid? :ex/non-small-vector-or-list-non-neg-even '[2 4 6]) ; True
(s/valid? :ex/non-small-vector-or-list-non-neg-even '[-2 -4 6]) ; False because it has negative numbers
(s/valid? :ex/non-small-vector-or-list-non-neg-even '[2 4]) ; False because less than 3 elements
(s/valid? :ex/non-small-vector-or-list-non-neg-even '(2 4 6)) ; True
(s/valid? :ex/non-small-vector-or-list-non-neg-even '(2 3 6)) ; False because 3 is odd
(s/valid? :ex/non-small-vector-or-list-non-neg-even '(2 4)) ; ; False because less than 3 elements
(s/valid? :ex/non-small-vector-or-list-non-neg-even #{2 4 6}) ; False because it is a set
(gen/generate (s/gen :ex/non-small-vector-or-list-non-neg-even))

;; A map of exactly 3 key-value pairs in which keys are strings no longer than 3 characters and values are of any type, but not nil or false.
;; three-pair-map consists of 3 kvpairsof key: string of length <=3 and value: truthy
;; Note: map-of https://clojuredocs.org/clojure.spec.alpha/map-of
;; map-of Function Signature: (map-of kpred vpred & opts)
;; (s/def :ex/three-kvpair-map (s/map-of #(and string? #(%.length < 0)) #(if %))) original attempt
(defn not-nil-or-false?[x]
  (not (or nil? false?)))
(not-nil-or-false? false)
(s/def :ex/three-kvpair-map (s/map-of (s/and string? #(<= (count %) 3)) ; #(<= (count %) 3) checks that string length is <= 3
                                      not-nil-or-false?
                                      :count 3))
(s/valid? :ex/three-kvpair-map {"abc" 123 "xyz" 987 "hi!" "hello"}); True
(s/valid? :ex/three-kvpair-map {"3rd" 3 "2nd" 2 "1st" 1}); True
(s/valid? :ex/three-kvpair-map {"Fred" 21 "Jaime" 47 "Echidona" 90}); false keys are strings longer than 3 characters.
(s/valid? :ex/three-kvpair-map {"a" "1st" "b" "2nd"}); false less than 3 kvpairs
(s/valid? :ex/three-kvpair-map {"a" 1 "b" 2 "c" 3 "d" 4}); false more than 3 kvpairs
(s/valid? :ex/three-kvpair-map #{"abc" 123 "xyz" 987 "hi!" "hello"}); false not a map
(gen/generate (s/gen :ex/three-kvpair-map))
;; A map in which keys are one of :a, :b, :c, :d, :e and keys are keywords different from these keys.
;; I will move forward under the assumption it means valies are keywords different than these keys.
(s/def :ex/abcde-key-map (s/map-of #{:a :b :c :d :e}
                                   (s/and keyword? #(not (contains? #{:a :b :c :d :e} %) )))) ;; Can remove contains?
;;Updated to remove contains?
(s/def :ex/abcde-key-map (s/map-of #{:a :b :c :d :e}
                                   (s/and keyword? #(not (#{:a :b :c :d :e} %)))))
(s/valid? :ex/abcde-key-map {:a :apple :b :banana :c :cherry}); true
(s/valid? :ex/abcde-key-map {:d :daisy :e :edelweiss}); true
(s/valid? :ex/abcde-key-map {:d :e}); false :e is one of the key values
(s/valid? :ex/abcde-key-map {:a "a" :b "b" :c "c"}); false "a" "b" and "c" are not keywords
(s/valid? :ex/abcde-key-map {:a :a :b :b :c :c}); false :a :b and :c can't be values
(s/valid? :ex/abcde-key-map #{:a :apple :b :banana}); false, it is not a map


;; A tuple of two strings and a number (in that order).
(s/def :ex/two-strings-number-tuple (s/tuple string? string? number?))

(s/valid? :ex/two-strings-number-tuple ["string1" "string2" 1]); True
(s/valid? :ex/two-strings-number-tuple ["aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa" "bbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbb" -20]); True
(s/valid? :ex/two-strings-number-tuple [:keyword "string1" 1]); False, has a keyword instead of a string
(s/valid? :ex/two-strings-number-tuple '("a" "b" 1)); False, not a tuple

(gen/generate (s/gen :ex/two-strings-number-tuple))

;; Sequences:
;; A sequence of an odd number of keywords.  
(s/def :ex/seq-oddkw (s/and seqable? 
                            (s/coll-of keyword?) 
                            #(odd? (count %))))

(s/valid? :ex/seq-oddkw '(:one :two :three)); True
(s/valid? :ex/seq-oddkw '(:one)); True
(s/valid? :ex/seq-oddkw [:one]); True
(s/valid? :ex/seq-oddkw '(:one :two)); False, even number of keywords
(gen/generate (s/gen :ex/seq-oddkw))
;;=> Execution error (ExceptionInfo) at clojure.test.check.generators/such-that-helper (generators.cljc:320).
;;   Couldn't satisfy such-that predicate after 100 tries.

;; A sequence of strings, each string of even length. 
(s/def :ex/seq-evenlength-strings (s/and seqable?
                                     (s/coll-of (s/and string? #(even? (count %))))))
(s/valid? :ex/seq-evenlength-strings '("hi" "even")); True
(s/valid? :ex/seq-evenlength-strings '("" "" "even")); True
(s/valid? :ex/seq-evenlength-strings '("odd" "false")); False, strings have odd length
(s/valid? :ex/seq-evenlength-strings '(1)); False, sequence of numbers
(s/valid? :ex/seq-evenlength-strings '(:kw)); False, sequence of keywords

;; A sequence that has a keyword :hello as one of its elements.
;; (s/def :ex/hellokw-seq (s/and seqable?
;;                               (s/coll-of any?)
;;                               #(contains? % :hello)))
(s/def :ex/hellokw-seq (s/and seqable?
                              (s/coll-of any?)
                              #(some #{:hello} %)))
(s/explain :ex/hellokw-seq '(1 2 3 :hello)); Success
(s/valid? :ex/hellokw-seq '(1 2 3 :hello)); True
(s/valid? :ex/hellokw-seq '[:hello]); True
(s/valid? :ex/hellokw-seq '[]); False
(s/valid? :ex/hellokw-seq '("hello")); False

;; A sequence of numbers and strings in which every number is followed by a string. For instance, [“a” “b” 1 “c”] would be valid. An empty sequence works, so does a sequence of any number of strings.
;; A non-empty sequence of numbers and strings that starts with a number in which every number is followed by at least one string.
