(ns tictactoe.core
  (:require [reagent.core :as reagent :refer [atom]]
            [clojure.pprint :as p]))

(enable-console-print!)

(def board-size 4)
(def circle-radius 0.45)
(def board-index (range board-size))

(defn new-board [n]
  (vec (repeat n (vec (repeat n "O")))))

(def initial-state
  {:text "Welcome to tic-tac-toe!!"
   :board (new-board board-size)
   :player "A"
   :game-state :running})

(defonce app-state (atom initial-state))

(defn new-game []
  (reset! app-state initial-state))

(defn next-player [p]
  (case p
    "A" "B"
    "B" "A"))

(defn check [position]
  (prn position)
  (reduce #(and %1 %2)
          (map (fn [[x y]]
                 (= (:player @app-state)
                    (get-in @app-state [:board x y])))
               position)))

(defn check-row []
  (reduce #(or %1 %2)
          (for [i board-index]
            (check (map (fn [j] [i j]) board-index)))))

(defn check-column []
  (reduce #(or %1 %2)
          (for [i board-index]
            (check (map (fn [j] [j i]) board-index)))))

(defn check-cross []
  (or (check (for [i board-index] [i i]))
      (check (for [i board-index] [i (- board-size i 1)]))))

(defn game-over?
  []
  (or (check-row)
      (check-column)
      (check-cross)))

(defn game-over
  []
  (swap! app-state assoc-in [:text]
         (str "The winner is: " (:player @app-state)))
  (swap! app-state assoc-in [:game-state]
         :end))

(defn onclick [i j]
  (if (= (:game-state @app-state) :end)
    (fn [e] nil)
    (fn [e]
      (swap! app-state assoc-in [:board j i] (:player @app-state))
      (if (game-over?) (game-over))
      (swap! app-state update-in [:player] next-player))))

(defn blank [i j]
  [:rect
   {:width 0.9
    :height 0.9
    :fill "grey"
    :x i
    :y j
    :on-click (onclick i j)}])

(defn circle [i j]
  [:circle
   {:r circle-radius
    :fill "yellow"
    :cx (+ circle-radius i)
    :cy (+ circle-radius j)}])

(defn cross [i j]
  [:g {:stroke "black"
       :stroke-width 0.4
       :stroke-linecap "round"
       :transform
       (str "translate(" (+ 0.5 i) "," (+ 0.5 j) ") "
            "scale(0.3)")}
   [:line {:x1 -1 :y1 -1 :x2 1 :y2 1}]
   [:line {:x1 -1 :y1 1 :x2 1 :y2 -1}]])

(defn tic-tac-toe []
  [:center
   [:h1 (:text @app-state)]
   [:p
    [:button
     {:on-click new-game}
     "New Game"]]
   (into
    [:svg
     {:view-box (str "0 0 " board-size " " board-size)
      :width 500
      :height 500}]
    (for [i (range board-size)
          j (range board-size)]
      (case (get-in @app-state [:board j i])
        "O" (blank i j)
        "A" (circle i j)
        "B" (cross i j))))])

(reagent/render-component [tic-tac-toe]
                          (. js/document (getElementById "app")))

(defn on-js-reload []
  (swap! app-state assoc-in [:text] "Welcome to tic-tac-toe!!")
  )
