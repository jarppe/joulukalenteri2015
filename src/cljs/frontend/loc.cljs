(ns frontend.loc)

(def terms {"fi" {:title      "Millan Joulukalenteri 2015"
                  :help       "Etsi luukkuja hiirellä. Voit avata tämän ja edellisten päivien luukkut hiiren painalluksella."
                  :art-copy   "Taide Copyright \u00A9 2013 Milla Länsiö"
                  :code       "Koodi"
                  :code-copy  "Copyright \u00A9 2015 Jarppe Länsiö"
                  :until      {1 "Tämän luukun voit avata jo huomenna"
                               2 "Enää kaksi yötä niin saat avata tämän luukun"
                               3 "Vielä kolme yötä tähän luukkuun"
                               4 "Vielä neljä yötä tähän luukkuun"
                               5 "Vielä viisi yötä tähän luukkuun"}
                  :until-more (fn [v] (str "Vielä " v " yötä tähän luukkuun"))}
            "en" {:title      "Milla's Christmas Calendar 2015"
                  :help       "Search for windows with your mouse. You can open windows for today and past days."
                  :art-copy   "Art Copyright \u00A9 2013 Milla Länsiö"
                  :code       "Code"
                  :code-copy  "Copyright \u00A9 2015 Jarppe Länsiö"
                  :until      {1 "This window you can open tomorrow"
                               2 "Just two nights until this window"
                               3 "Three nights and then you can open this window"
                               4 "Four nights and then you can open this window"
                               5 "Five nights and then you can open this window"}
                  :until-more (fn [v] (str "Just " v " nights until you can open this window"))}})
