package com.example.learning.content

import com.example.core.database.Dictation
import com.example.core.database.Exercise
import com.example.core.database.VocabularyWord

object ExercisesAndDictationsSeed {

    fun exercises(): List<Exercise> = listOf(
        // 1. QCM
        Exercise(
            id = "ex_qcm_01", lessonId = "a1_ort_01", level = "A1", category = "Orthographe",
            type = "QCM", instruction = "Choisissez l'orthographe exacte du mot manquant.",
            promptText = "Les étudiants apprennent le ___ chaque matin.",
            optionsPipe = "francais|français|françai|fransais",
            expectedAnswer = "français",
            explanation = "Devant « a », la cédille est obligatoire sur le « c » pour produire le son [s] : « français ».",
            points = 10, difficulty = 1
        ),
        Exercise(
            id = "ex_qcm_02", lessonId = "a2_gram_01", level = "A2", category = "Grammaire",
            type = "QCM", instruction = "Sélectionnez le bon homophone grammatical.",
            promptText = "Ils ___ partis très tôt ce matin.",
            optionsPipe = "son|sont|ont|est",
            expectedAnswer = "sont",
            explanation = "On peut remplacer par « étaient » : il s'agit de l'auxiliaire être conjugué « sont ».",
            points = 10, difficulty = 2
        ),
        // 2. TEXTE_A_TROUS
        Exercise(
            id = "ex_trou_01", lessonId = "a1_gram_01", level = "A1", category = "Grammaire",
            type = "TEXTE_A_TROUS", instruction = "Complétez le trou avec le déterminant possessif correct.",
            promptText = "Il travaille dans ___ entreprise depuis trois ans.",
            optionsPipe = "",
            expectedAnswer = "son",
            explanation = "Devant un nom féminin singulier commençant par une voyelle (« entreprise »), on emploie « son » au lieu de « sa » par euphonie.",
            points = 12, difficulty = 2
        ),
        Exercise(
            id = "ex_trou_02", lessonId = "b1_gram_01", level = "B1", category = "Grammaire",
            type = "TEXTE_A_TROUS", instruction = "Accordez correctement le participe passé du verbe « acheter ».",
            promptText = "Les fleurs que j'ai ___ embaument la pièce.",
            optionsPipe = "",
            expectedAnswer = "achetées",
            explanation = "Le COD « les fleurs » (féminin pluriel) est placé avant le verbe avec « avoir » : le participe passé s'accorde en « achetées ».",
            points = 15, difficulty = 3
        ),
        // 3. VRAI_FAUX
        Exercise(
            id = "ex_vf_01", lessonId = "a2_conj_01", level = "A2", category = "Grammaire",
            type = "VRAI_FAUX", instruction = "Indiquez si l'affirmation suivante est vraie ou fausse.",
            promptText = "Dans la phrase « Elle a mangé. », le participe passé doit prendre un « e » car le sujet est féminin.",
            optionsPipe = "Vrai|Faux",
            expectedAnswer = "Faux",
            explanation = "Faux ! Avec l'auxiliaire « avoir », le participe passé ne s'accorde jamais avec le sujet. Sans COD antéposé, « mangé » reste invariable.",
            points = 10, difficulty = 2
        ),
        Exercise(
            id = "ex_vf_02", lessonId = "b2_conj_01", level = "B2", category = "Conjugaison",
            type = "VRAI_FAUX", instruction = "Indiquez si la phrase suivante est grammaticalement correcte.",
            promptText = "« Si j'avais su, je serais venu. » est une phrase parfaitement correcte.",
            optionsPipe = "Vrai|Faux",
            expectedAnswer = "Vrai",
            explanation = "Vrai ! L'irréel du passé se construit avec « Si + plus-que-parfait » suivi du « conditionnel passé ».",
            points = 12, difficulty = 3
        ),
        // 4. ASSOCIATION
        Exercise(
            id = "ex_assoc_01", lessonId = "a2_gram_01", level = "A2", category = "Grammaire",
            type = "ASSOCIATION", instruction = "Associez la bonne règle au couple d'homophones « leur / leurs ».",
            promptText = "Comment distinguer « leur » et « leurs » dans « Leur maison est grande » ?",
            optionsPipe = "« leur » est singulier devant un nom singulier|« leur » prend toujours un s|« leurs » s'utilise devant un verbe|« leur » est un verbe",
            expectedAnswer = "« leur » est singulier devant un nom singulier",
            explanation = "Devant le nom singulier « maison », le déterminant possessif reste au singulier : « Leur maison est grande ».",
            points = 12, difficulty = 2
        ),
        // 5. CORRECTION_PHRASE
        Exercise(
            id = "ex_corr_01", lessonId = "a1_conj_01", level = "A1", category = "Conjugaison",
            type = "CORRECTION_PHRASE", instruction = "Réécrivez la phrase entière en corrigeant l'erreur d'accord.",
            promptText = "Les enfants joue dans la cour.",
            optionsPipe = "",
            expectedAnswer = "Les enfants jouent dans la cour.",
            acceptedAnswersPipe = "Les enfants jouent dans la cour",
            explanation = "Le sujet « Les enfants » est à la 3e personne du pluriel : le verbe prend la terminaison « -ent » (« jouent »).",
            points = 15, difficulty = 2
        ),
        Exercise(
            id = "ex_corr_02", lessonId = "a2_conj_01", level = "A2", category = "Grammaire",
            type = "CORRECTION_PHRASE", instruction = "Corrigez la confusion infinitif / participe passé.",
            promptText = "Je vais travaillé.",
            optionsPipe = "",
            expectedAnswer = "Je vais travailler.",
            acceptedAnswersPipe = "Je vais travailler",
            explanation = "Après le semi-auxiliaire « vais » (futur proche), le verbe se met à l'infinitif : « Je vais travailler. »",
            points = 15, difficulty = 2
        ),
        // 6. CONJUGAISON
        Exercise(
            id = "ex_conj_01", lessonId = "b1_conj_01", level = "B1", category = "Conjugaison",
            type = "CONJUGAISON", instruction = "Conjuguez le verbe « venir » au subjonctif présent (2e pers. sing.).",
            promptText = "Il faut que tu (venir) ___ demain.",
            optionsPipe = "",
            expectedAnswer = "viennes",
            explanation = "Après « il faut que », on utilise le subjonctif présent : « que tu viennes ».",
            points = 15, difficulty = 3
        ),
        Exercise(
            id = "ex_conj_02", lessonId = "a1_conj_01", level = "A2", category = "Conjugaison",
            type = "CONJUGAISON", instruction = "Conjuguez « apprendre » au présent de l'indicatif (3e pers. pluriel).",
            promptText = "Les étudiants (apprendre) ___ le français.",
            optionsPipe = "",
            expectedAnswer = "apprennent",
            explanation = "À la 3e personne du pluriel au présent, « apprendre » double le « n » : « ils apprennent ».",
            points = 12, difficulty = 2
        ),
        // 7. DICTEE (courte en exercice)
        Exercise(
            id = "ex_dict_01", lessonId = "a2_ponct_01", level = "B1", category = "Orthographe",
            type = "DICTEE", instruction = "Écrivez sans faute la phrase modèle (accents et ponctuation inclus).",
            promptText = "Phrase à mémoriser et recopier exactement : « Leur maison est grande. »",
            optionsPipe = "",
            expectedAnswer = "Leur maison est grande.",
            acceptedAnswersPipe = "Leur maison est grande",
            explanation = "« Leur » reste au singulier devant « maison » (singulier), « est » est le verbe être, et « grande » s'accorde au féminin.",
            points = 12, difficulty = 2
        ),
        // 8. REECRITURE
        Exercise(
            id = "ex_reec_01", lessonId = "b2_ecrit_01", level = "B2", category = "Style",
            type = "REECRITURE", instruction = "Remplacez la tournure fautive « malgré que » par une locution normative.",
            promptText = "Malgré qu'il soit tard, nous continuons.",
            optionsPipe = "Bien qu'il soit tard, nous continuons.|Malgré il est tard, nous continuons.|Parce qu'il soit tard, nous continuons.|Du coup il est tard, nous continuons.",
            expectedAnswer = "Bien qu'il soit tard, nous continuons.",
            explanation = "« Bien que » suivi du subjonctif remplace élégamment « malgré que ».",
            points = 15, difficulty = 3
        ),
        // 9. COMPREHENSION
        Exercise(
            id = "ex_comp_01", lessonId = "b1_comp_01", level = "B1", category = "Compréhension",
            type = "COMPREHENSION", instruction = "Lisez l'extrait et identifiez la relation logique exprimée.",
            promptText = "« Le projet comportait de nombreux défis techniques ; néanmoins, l'équipe a tenu ses engagements. » Quel lien introduit « néanmoins » ?",
            optionsPipe = "Une cause|Une concession / opposition|Une conséquence|Une énumération",
            expectedAnswer = "Une concession / opposition",
            explanation = "« Néanmoins » (comme « cependant » ou « toutefois ») marque une opposition nuancée ou une concession.",
            points = 12, difficulty = 3
        ),
        // 10. IDENTIFICATION_ERREUR
        Exercise(
            id = "ex_ident_01", lessonId = "c1_vocab_01", level = "C1", category = "Syntaxe",
            type = "IDENTIFICATION_ERREUR", instruction = "Identifiez le mot ou groupe fautif dans la phrase suivante.",
            promptText = "« Au jour d'aujourd'hui, cette méthode nous aide à pallier à nos difficultés. » Quel double défaut apparaît ?",
            optionsPipe = "Un pléonasme et une préposition en trop après pallier|Une faute de conjugaison|Un oubli de cédille|Un mauvais pronom sujet",
            expectedAnswer = "Un pléonasme et une préposition en trop après pallier",
            explanation = "« Au jour d'aujourd'hui » est un pléonasme (préférez « aujourd'hui ») et « pallier » se construit sans « à » (« pallier nos difficultés »).",
            points = 20, difficulty = 4
        ),
        Exercise(
            id = "ex_ident_02", lessonId = "c2_style_01", level = "C2", category = "Grammaire",
            type = "QCM", instruction = "Choisissez l'accord exact de l'adverbe « tout ».",
            promptText = "À l'annonce du résultat, elle était ___ émue et ___ surprise.",
            optionsPipe = "tout / toute|toute / tout|toute / toute|tout / tout",
            expectedAnswer = "tout / toute",
            explanation = "L'adverbe « tout » reste invariable devant une voyelle (« tout émue ») mais varie par euphonie devant un adjectif féminin commençant par une consonne (« toute surprise »).",
            points = 20, difficulty = 5
        ),
        // Quiz dédiés aux chapitres du parcours progressif A1 → C2
        Exercise(
            id = "ex_path_a1_gram", lessonId = "a1_gram_01", level = "A1", category = "Grammaire",
            type = "QCM", instruction = "Choisissez le déterminant possessif correct devant voyelle.",
            promptText = "Elle présente ___ histoire avec beaucoup de clarté.",
            optionsPipe = "sa|son|ses|la",
            expectedAnswer = "son",
            explanation = "Devant un nom féminin singulier commençant par un « h » muet (« histoire »), on emploie « son » par euphonie.",
            points = 10, difficulty = 1
        ),
        Exercise(
            id = "ex_path_a1_conj", lessonId = "a1_conj_01", level = "A1", category = "Conjugaison",
            type = "QCM", instruction = "Sélectionnez la forme correcte au présent de l'indicatif.",
            promptText = "Les apprenants ___ leurs exercices chaque soir.",
            optionsPipe = "révise|révises|révisent|révisons",
            expectedAnswer = "révisent",
            explanation = "Avec un sujet à la 3e personne du pluriel (« Les apprenants »), les verbes du 1er groupe prennent « -ent ».",
            points = 10, difficulty = 1
        ),
        Exercise(
            id = "ex_path_a1_synt", lessonId = "a1_synt_01", level = "A1", category = "Syntaxe",
            type = "QCM", instruction = "Choisissez la forme négative correcte.",
            promptText = "Transformez à la négative : « J'ai un doute. »",
            optionsPipe = "Je n'ai pas un doute.|Je n'ai pas de doute.|Je ai pas de doute.|Je ne pas ai de doute.",
            expectedAnswer = "Je n'ai pas de doute.",
            explanation = "Après une négation (hors verbe être), l'article indéfini « un » devient « de » : « Je n'ai pas de doute. »",
            points = 10, difficulty = 1
        ),
        Exercise(
            id = "ex_path_a2_imp", lessonId = "a2_conj_02", level = "A2", category = "Conjugaison",
            type = "QCM", instruction = "Choisissez l'imparfait exact du verbe « étudier ».",
            promptText = "L'an dernier, nous ___ ensemble à la bibliothèque.",
            optionsPipe = "étudions|étudiions|étudiait|étudiez",
            expectedAnswer = "étudiions",
            explanation = "Radical « étudi- » (de nous étudions) + terminaison « -ions » = « nous étudiions » (avec deux « i »).",
            points = 12, difficulty = 2
        ),
        Exercise(
            id = "ex_path_a2_ponct", lessonId = "a2_ponct_01", level = "A2", category = "Ponctuation",
            type = "QCM", instruction = "Repérez la phrase correctement ponctuée.",
            promptText = "Quelle phrase respecte les règles de ponctuation française ?",
            optionsPipe = "Les étudiants, travaillent sérieusement.|Les étudiants travaillent ; ils progressent vite.|Les étudiants travaillent , ils progressent.|les étudiants travaillent vite.",
            expectedAnswer = "Les étudiants travaillent ; ils progressent vite.",
            explanation = "On ne sépare jamais le sujet du verbe par une virgule unique, et la phrase commence par une majuscule.",
            points = 12, difficulty = 2
        ),
        Exercise(
            id = "ex_path_b1_pp", lessonId = "b1_gram_01", level = "B1", category = "Grammaire",
            type = "QCM", instruction = "Accordez le participe passé avec l'auxiliaire « avoir ».",
            promptText = "Voici les règles que nous avons ___ cette semaine.",
            optionsPipe = "appris|apprise|apprises|apprit",
            expectedAnswer = "apprises",
            explanation = "Le COD « les règles » (féminin pluriel) est placé avant le verbe via « que » : accord en « apprises ».",
            points = 14, difficulty = 3
        ),
        Exercise(
            id = "ex_path_b1_subj", lessonId = "b1_conj_01", level = "B1", category = "Conjugaison",
            type = "QCM", instruction = "Complétez au mode exigé par la locution « bien que ».",
            promptText = "Bien qu'il ___ tard, il relit encore sa rédaction.",
            optionsPipe = "est|soit|sera|était",
            expectedAnswer = "soit",
            explanation = "La conjonction concessive « bien que » impose toujours le subjonctif : « bien qu'il soit tard ».",
            points = 14, difficulty = 3
        ),
        Exercise(
            id = "ex_path_b1_disc", lessonId = "b1_synt_01", level = "B1", category = "Syntaxe",
            type = "QCM", instruction = "Transposez au discours indirect au passé.",
            promptText = "Elle a dit : « Je terminerai demain. » → Elle a dit qu'elle ___.",
            optionsPipe = "terminera demain|terminerait le lendemain|a terminé hier|terminait demain",
            expectedAnswer = "terminerait le lendemain",
            explanation = "Avec un verbe introducteur au passé, le futur simple devient conditionnel présent et « demain » devient « le lendemain ».",
            points = 14, difficulty = 3
        ),
        Exercise(
            id = "ex_path_b2_pro", lessonId = "b2_pro_01", level = "B2", category = "Style",
            type = "QCM", instruction = "Choisissez l'accord normatif de « ci-joint ».",
            promptText = "Veuillez consulter les documents ___ à mon message.",
            optionsPipe = "ci-joint|ci-joints|ci-jointe|ci-jointes",
            expectedAnswer = "ci-joints",
            explanation = "Placé en épithète après le nom masculin pluriel « documents », « ci-joints » s'accorde en genre et en nombre.",
            points = 15, difficulty = 3
        ),
        Exercise(
            id = "ex_path_b2_pass", lessonId = "b2_synt_01", level = "B2", category = "Syntaxe",
            type = "QCM", instruction = "Identifiez la mise en relief correcte.",
            promptText = "Mettez en relief le sujet dans « La régularité fait la différence ».",
            optionsPipe = "C'est la régularité que fait la différence.|C'est la régularité qui fait la différence.|Ce qui la régularité fait la différence.|C'est de la régularité qui fait la différence.",
            expectedAnswer = "C'est la régularité qui fait la différence.",
            explanation = "Pour mettre en valeur un sujet, on utilise la structure clivée « C'est ... qui ».",
            points = 15, difficulty = 3
        ),
        Exercise(
            id = "ex_path_c1_rel", lessonId = "c1_synt_01", level = "C1", category = "Syntaxe",
            type = "QCM", instruction = "Sélectionnez le pronom relatif approprié à la construction du verbe.",
            promptText = "Voici l'objectif ___ nous consacrons toute notre énergie.",
            optionsPipe = "dont|auquel|duquel|que",
            expectedAnswer = "auquel",
            explanation = "On consacre son énergie À un objectif (masculin singulier) : le pronom relatif composé est donc « auquel ».",
            points = 18, difficulty = 4
        ),
        Exercise(
            id = "ex_path_c1_conc", lessonId = "c1_ecrit_01", level = "C1", category = "Style",
            type = "QCM", instruction = "Distinguez « quoique » et « quoi que ».",
            promptText = "___ vous entrepreniez, la méthode reste votre meilleure alliée.",
            optionsPipe = "Quoique|Quoi que|Malgré que|Bien que",
            expectedAnswer = "Quoi que",
            explanation = "« Quoi que » en deux mots signifie « quelle que soit la chose que » (suivi du subjonctif « entrepreniez »).",
            points = 18, difficulty = 4
        ),
        Exercise(
            id = "ex_path_c1_pro", lessonId = "c1_pro_01", level = "C1", category = "Style",
            type = "QCM", instruction = "Choisissez le verbe le plus précis pour une note de synthèse.",
            promptText = "Les mesures d'audience ___ les conclusions de l'étude préliminaire.",
            optionsPipe = "font vrai|corroborent|mettent bien|disent pareil",
            expectedAnswer = "corroborent",
            explanation = "« Corroborer » signifie confirmer et renforcer une analyse par de nouveaux éléments factuels.",
            points = 18, difficulty = 4
        ),
        Exercise(
            id = "ex_path_c2_oral", lessonId = "c2_oral_01", level = "C2", category = "Compréhension",
            type = "QCM", instruction = "Repérez la liaison interdite en français standard.",
            promptText = "Dans quel groupe la liaison est-elle strictement interdite ?",
            optionsPipe = "les_enfants|ils_apprennent|un grand_homme|des // héros",
            expectedAnswer = "des // héros",
            explanation = "Le mot « héros » commence par un « h » aspiré qui interdit toute liaison et toute élision (« le héros », « des // héros »).",
            points = 20, difficulty = 5
        ),
        Exercise(
            id = "ex_path_c2_rhet", lessonId = "c2_style_02", level = "C2", category = "Style",
            type = "QCM", instruction = "Identifiez la figure de style employée.",
            promptText = "« Ce progrès n'est pas négligeable » pour dire « Ce progrès est remarquable » est :",
            optionsPipe = "Une anaphore|Une litote|Un pléonasme|Une hyperbole",
            expectedAnswer = "Une litote",
            explanation = "La litote consiste à dire moins pour laisser entendre beaucoup plus avec retenue et force.",
            points = 20, difficulty = 5
        ),
        Exercise(
            id = "ex_path_c2_vocab", lessonId = "c2_vocab_01", level = "C2", category = "Vocabulaire",
            type = "QCM", instruction = "Choisissez la formulation exempte d'impropriété et de calque.",
            promptText = "Quelle phrase relève d'un français éditorial irréprochable ?",
            optionsPipe = "Cette analyse fait sens et s'avère vraie.|Cette analyse est fondée sur des faits et a pleinement du sens.|Cette analyse est basée sur du concret définitivement.|Cette analyse risque de réussir rapidement.",
            expectedAnswer = "Cette analyse est fondée sur des faits et a pleinement du sens.",
            explanation = "On évite le calque « faire sens », le pléonasme « s'avérer vrai » et l'emploi impropre de « risquer » pour un événement positif.",
            points = 20, difficulty = 5
        )
    )

    fun dictations(): List<Dictation> = listOf(
        Dictation(
            id = "dict_a1_01", title = "Matinée à l'école", level = "A1", theme = "Vie quotidienne",
            referenceText = "Les enfants jouent dans la cour. Les étudiants apprennent le français avec sérieux. Leur maison est grande et lumineuse.",
            keyRulesPipe = "Accord sujet-verbe au pluriel|Cédille de français|Leur devant nom singulier",
            wordCount = 20
        ),
        Dictation(
            id = "dict_a2_01", title = "Une journée de travail", level = "A2", theme = "Travail",
            referenceText = "Il travaille dans son entreprise depuis deux années. Elle a mangé rapidement, puis ils sont partis ensemble. Je vais travailler sur ce projet demain.",
            keyRulesPipe = "Son devant voyelle (son entreprise)|Passé composé avoir vs être|Infinitif après vais",
            wordCount = 25
        ),
        Dictation(
            id = "dict_b1_01", title = "Le bouquet et la promesse", level = "B1", theme = "Accords et subjonctif",
            referenceText = "Les fleurs que j'ai achetées illuminent la table. Il faut que tu viennes nous rendre visite ce week-end, car nous avons préparé une belle surprise.",
            keyRulesPipe = "Participe passé avec COD antéposé (achetées)|Subjonctif après il faut que (viennes)|Homophone car",
            wordCount = 26
        ),
        Dictation(
            id = "dict_b2_01", title = "L'art de la nuance", level = "B2", theme = "Hypothèse et style",
            referenceText = "Si j'avais su, je serais venu dès l'ouverture de la conférence. Bien que le sujet soit complexe, l'orateur a présenté une synthèse remarquablement claire et rigoureuse.",
            keyRulesPipe = "Concordance Si + plus-que-parfait -> conditionnel passé|Subjonctif après bien que|Accords adjectifs",
            wordCount = 27
        ),
        Dictation(
            id = "dict_c1_01", title = "Rigueur et précision", level = "C1", theme = "Français professionnel",
            referenceText = "La conjoncture actuelle exige des décisions réfléchies. Les équipes qui se sont succédé ont su pallier les obstacles techniques tout en préservant l'excellence de leur travail.",
            keyRulesPipe = "Paronyme conjoncture|Invariabilité de se succéder|Transitivité directe de pallier",
            wordCount = 26
        )
    )

    fun vocabularyWords(): List<VocabularyWord> = listOf(
        VocabularyWord(
            id = "voc_01", word = "Perspicace", level = "B2", theme = "Qualités intellectuelles",
            definition = "Qui fait preuve d'une grande finesse d'esprit, capable de percevoir ce qui échappe aux autres.",
            synonymsPipe = "Clairvoyant|Lucide|Pénétrant", antonymsPipe = "Naïf|Aveugle",
            exampleSentence = "Son analyse perspicace a permis de résoudre le problème en quelques minutes.",
            quizQuestion = "Quel est le meilleur synonyme de « perspicace » ?",
            quizOptionsPipe = "Clairvoyant|Hésitant|Bruyant|Superficiel", quizAnswer = "Clairvoyant"
        ),
        VocabularyWord(
            id = "voc_02", word = "Pallier", level = "C1", theme = "Français professionnel",
            definition = "Remédier provisoirement ou compenser un manque, une difficulté (transitif direct : pallier quelque chose).",
            synonymsPipe = "Compenser|Atténuer|Remédier à", antonymsPipe = "Aggraver|Accentuer",
            exampleSentence = "Ce dispositif hors ligne permet de pallier l'absence de connexion Internet.",
            quizQuestion = "Quelle construction avec « pallier » est correcte ?",
            quizOptionsPipe = "Pallier un manque|Pallier à un manque|Pallier de un manque|Pallier sur un manque",
            quizAnswer = "Pallier un manque"
        ),
        VocabularyWord(
            id = "voc_03", word = "Éloquent", level = "B2", theme = "Expression orale",
            definition = "Qui s'exprime avec aisance et force de persuasion ; qui est très significatif.",
            synonymsPipe = "Convaincant|Expressif|Parlant", antonymsPipe = "Hésitant|Confus|Muet",
            exampleSentence = "Les chiffres de progression sont particulièrement éloquents.",
            quizQuestion = "Que signifie un résultat « éloquent » ?",
            quizOptionsPipe = "Très significatif et parlant|Incompréhensible|Très long|Sans intérêt",
            quizAnswer = "Très significatif et parlant"
        ),
        VocabularyWord(
            id = "voc_04", word = "Concis", level = "B1", theme = "Rédaction",
            definition = "Qui s'exprime en peu de mots tout en restant clair et complet.",
            synonymsPipe = "Bref|Synthétique|Sobriété", antonymsPipe = "Prolixe|Verbeux|Bavard",
            exampleSentence = "Un courriel professionnel gagne toujours à rester concis.",
            quizQuestion = "Quel est l'antonyme de « concis » ?",
            quizOptionsPipe = "Verbeux|Précis|Clair|Rapide", quizAnswer = "Verbeux"
        ),
        VocabularyWord(
            id = "voc_05", word = "Pérenniser", level = "C1", theme = "Français professionnel",
            definition = "Rendre durable, assurer la continuité dans le temps.",
            synonymsPipe = "Consolider|Inscrire dans la durée|Maintenir", antonymsPipe = "Interrompre|Fragiliser",
            exampleSentence = "Une pratique quotidienne de dix minutes aide à pérenniser les acquis.",
            quizQuestion = "Que signifie « pérenniser une méthode » ?",
            quizOptionsPipe = "La rendre durable dans le temps|La supprimer rapidement|La traduire|La critiquer",
            quizAnswer = "La rendre durable dans le temps"
        ),
        VocabularyWord(
            id = "voc_06", word = "Nuance", level = "A2", theme = "Communication",
            definition = "Différence subtile et délicate entre deux idées, deux mots ou deux couleurs.",
            synonymsPipe = "Subtilité|Finesse|Teinte", antonymsPipe = "Contraste brutal|Caricature",
            exampleSentence = "Apprendre des synonymes permet d'exprimer chaque nuance de sa pensée.",
            quizQuestion = "Que désigne une « nuance » dans un texte ?",
            quizOptionsPipe = "Une différence subtile de sens|Une faute de frappe|Un titre en majuscules|Un paragraphe vide",
            quizAnswer = "Une différence subtile de sens"
        ),
        VocabularyWord(
            id = "voc_07", word = "Exhaustif", level = "C1", theme = "Analyse",
            definition = "Qui traite un sujet à fond, sans rien omettre.",
            synonymsPipe = "Complet|Intégral|Approfondi", antonymsPipe = "Partiel|Lacunaire|Incomplet",
            exampleSentence = "Ce guide présente un inventaire exhaustif des règles d'accord.",
            quizQuestion = "Quel mot signifie le contraire d'« exhaustif » ?",
            quizOptionsPipe = "Lacunaire|Complet|Total|Précis", quizAnswer = "Lacunaire"
        ),
        VocabularyWord(
            id = "voc_08", word = "Bénévole", level = "B1", theme = "Société",
            definition = "Qui agit gratuitement, sans rémunération et de plein gré.",
            synonymsPipe = "Volontaire|Désintéressé", antonymsPipe = "Rémunéré|Salarié",
            exampleSentence = "Plusieurs formateurs bénévoles animent des ateliers d'écriture locaux.",
            quizQuestion = "Une action bénévole est réalisée :",
            quizOptionsPipe = "Gratuitement et librement|Contre un abonnement|Sous contrainte|Uniquement en ligne",
            quizAnswer = "Gratuitement et librement"
        ),
        VocabularyWord(
            id = "voc_09", word = "Subtil", level = "B2", theme = "Style",
            definition = "Fin, délicat, difficile à saisir au premier regard.",
            synonymsPipe = "Fin|Délicat|Ingénieux", antonymsPipe = "Grossier|Évident|Lourd",
            exampleSentence = "La différence entre l'imparfait et le passé simple est parfois subtile.",
            quizQuestion = "Quel est un synonyme de « subtil » ?",
            quizOptionsPipe = "Délicat|Bruyant|Visible|Brutal", quizAnswer = "Délicat"
        ),
        VocabularyWord(
            id = "voc_10", word = "Corroborer", level = "C2", theme = "Argumentation",
            definition = "Confirmer, appuyer une affirmation ou une hypothèse par de nouvelles preuves.",
            synonymsPipe = "Confirmer|Étayer|Conforter", antonymsPipe = "Infirmer|Contredire|Réfuter",
            exampleSentence = "Les derniers résultats viennent corroborer son hypothèse initiale.",
            quizQuestion = "Quel est l'antonyme exact de « corroborer » ?",
            quizOptionsPipe = "Infirmer|Confirmer|Valider|Approuver", quizAnswer = "Infirmer"
        )
    )
}
