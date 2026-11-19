package com.example.learning.content

import com.example.core.database.Lesson

data class PlacementQuestion(
    val id: String,
    val level: String,
    val skill: String,
    val question: String,
    val options: List<String>,
    val correctIndex: Int,
    val explanation: String
)

data class WritingTemplate(
    val id: String,
    val category: String,
    val title: String,
    val objective: String,
    val tips: List<String>,
    val starterDraft: String
)

data class SpeakingDrill(
    val id: String,
    val level: String,
    val focus: String,
    val sentence: String,
    val phoneticGuide: String,
    val coachingTip: String
)

object AcademyLessonsSeed {

    fun lessons(): List<Lesson> = listOf(
        // A1 — Débutant
        Lesson(
            id = "a1_ort_01", level = "A1", moduleTitle = "Fondamentaux d'orthographe", chapterOrder = 1,
            theme = "Orthographe", title = "Les accents et la cédille (é, è, ê, ç)",
            summary = "Maîtriser les trois accents du français et l'usage de la cédille devant a, o et u.",
            ruleContent = "En français, les accents modifient la prononciation ou distinguent des homophones :\n• L'accent aigu (é) note le son [e] fermé : école, étudiant, vérité.\n• L'accent grave (è) note le son [ɛ] ouvert (élève, problème, règle) et distingue « à/a », « où/ou ».\n• L'accent circonflexe (ê, ô, â, î, û) marque souvent un ancien « s » disparu : être, fête, forêt, bientôt.\n• La cédille (ç) s'emploie uniquement devant a, o, u pour conserver le son [s] : français, garçon, leçon, reçu.",
            examplesJson = "Les étudiants apprennent le français.||L'élève révise sa leçon après l'école.||Nous avons reçu un aperçu très clair.",
            pitfallsContent = "On ne met jamais de cédille devant « e » ou « i » (on écrit « merci », « cela »). On ne met pas d'accent sur un « e » suivi d'une double consonne (belle, effort).",
            estimatedMinutes = 7
        ),
        Lesson(
            id = "a1_gram_01", level = "A1", moduleTitle = "Bases grammaticales", chapterOrder = 2,
            theme = "Grammaire", title = "Déterminants, genre et nombre",
            summary = "Accorder correctement les articles, possessifs et démonstratifs avec le nom.",
            ruleContent = "Le déterminant s'accorde en genre (masculin/féminin) et en nombre (singulier/pluriel) avec le nom qu'il accompagne.\n• Élision : « le » et « la » deviennent « l' » devant une voyelle ou un h muet (l'école, l'entreprise).\n• Euphonie : devant un nom féminin singulier commençant par une voyelle ou un h muet, « ma, ta, sa » deviennent « mon, ton, son » : son entreprise, mon amie, ton histoire.",
            examplesJson = "Il travaille dans son entreprise.||Leur maison est grande.||Les enfants jouent dans la cour.",
            pitfallsContent = "Ne jamais écrire « sa entreprise » ou « ma amie ». Attention au genre de noms piégeux : un exercice, une erreur, un problème, une histoire.",
            estimatedMinutes = 8
        ),
        Lesson(
            id = "a1_conj_01", level = "A1", moduleTitle = "Verbes essentiels", chapterOrder = 3,
            theme = "Conjugaison", title = "Le présent de l'indicatif et les 3 groupes",
            summary = "Conjuguer les verbes courants au présent et accorder sujet et verbe.",
            ruleContent = "Les verbes français se répartissent en trois groupes :\n• 1er groupe (-er sauf aller) : -e, -es, -e, -ons, -ez, -ent (je travaille, ils jouent).\n• 2e groupe (-ir, participe présent -issant) : -is, -is, -it, -issons, -issez, -issent (nous finissons).\n• 3e groupe (irréguliers) : être, avoir, aller, faire, venir, prendre, apprendre.\nLe verbe s'accorde toujours en personne et en nombre avec son sujet.",
            examplesJson = "Les enfants jouent dans la cour.||Les étudiants apprennent le français.||Je vais travailler dès demain.",
            pitfallsContent = "À l'oral, « il joue » et « ils jouent » se prononcent pareil, mais le pluriel prend toujours « -nt » à l'écrit. Attention à « vous faites » et « vous dites ».",
            estimatedMinutes = 9
        ),
        // A2 — Élémentaire
        Lesson(
            id = "a2_gram_01", level = "A2", moduleTitle = "Homophones grammaticaux", chapterOrder = 1,
            theme = "Orthographe", title = "Homophones essentiels : a/à, et/est, son/sont, ou/où",
            summary = "Utiliser la méthode de substitution fiable pour ne plus confondre les homophones.",
            ruleContent = "La technique la plus fiable consiste à remplacer le mot par un équivalent grammatical :\n• a (verbe avoir) → remplaçable par « avait » ; à (préposition) → non remplaçable.\n• est (verbe être) → remplaçable par « était » ; et (conjonction) → remplaçable par « et puis ».\n• sont (verbe être) → remplaçable par « étaient » ; son (possessif) → remplaçable par « mon ».\n• ou (choix) → remplaçable par « ou bien » ; où (lieu/temps) → non remplaçable.",
            examplesJson = "Elle a mangé à midi.||Leur maison est grande et lumineuse.||Ils sont partis dans son entreprise.",
            pitfallsContent = "Distinction « leur / leurs » : devant un verbe, « leur » est un pronom invariable (Je leur parle). Devant un nom, il s'accorde : leur maison / leurs maisons.",
            estimatedMinutes = 8
        ),
        Lesson(
            id = "a2_conj_01", level = "A2", moduleTitle = "Temps du passé", chapterOrder = 2,
            theme = "Conjugaison", title = "Passé composé : choisir entre avoir et être",
            summary = "Maîtriser le choix de l'auxiliaire et l'accord de base du participe passé.",
            ruleContent = "La majorité des verbes se conjuguent avec « avoir ». Une quinzaine de verbes de mouvement ou d'état (aller, venir, arriver, partir, entrer, sortir, monter, descendre, naître, mourir, rester, tomber, devenir, revenir) ainsi que tous les verbes pronominaux utilisent « être ».\n• Avec « être », le participe passé s'accorde avec le sujet : Ils sont partis. Elle est arrivée.\n• Avec « avoir », le participe passé ne s'accorde jamais avec le sujet : Elle a mangé.",
            examplesJson = "Elle a mangé avant la réunion.||Ils sont partis très tôt.||Nous avons terminé l'exercice.",
            pitfallsContent = "Ne jamais confondre l'infinitif (-er) et le participe passé (-é). Test de remplacement : remplacez par « prendre / pris » (Je vais prendre → Je vais travailler ; Elle a pris → Elle a mangé).",
            estimatedMinutes = 10
        ),
        Lesson(
            id = "a2_ponct_01", level = "A2", moduleTitle = "Clarté typographique", chapterOrder = 3,
            theme = "Ponctuation", title = "Règles de ponctuation française",
            summary = "Maîtriser les virgules, points et règles d'espacement en français.",
            ruleContent = "La ponctuation structure le sens et la respiration de la phrase :\n• Signes simples (.,) : jamais d'espace avant, un espace après.\n• Signes doubles (; : ! ?) : en typographie française soignée, espace fine insécable avant et espace après.\n• Majuscule obligatoire au début de chaque phrase et aux noms propres.",
            examplesJson = "Bonjour, comment allez-vous ?||Les étudiants travaillent ; ils progressent vite.",
            pitfallsContent = "Évitez de séparer le sujet de son verbe par une virgule unique (incorrect : « Les étudiants, apprennent le français »).",
            estimatedMinutes = 6
        ),
        // B1 — Intermédiaire
        Lesson(
            id = "b1_gram_01", level = "B1", moduleTitle = "Accords avancés", chapterOrder = 1,
            theme = "Grammaire", title = "L'accord du participe passé avec avoir (COD antéposé)",
            summary = "Appliquer avec rigueur la règle du complément d'objet direct placé avant le verbe.",
            ruleContent = "Avec l'auxiliaire « avoir », le participe passé reste invariable, SAUF si le Complément d'Objet Direct (COD) est placé AVANT le verbe. Dans ce cas, il s'accorde en genre et en nombre avec ce COD.\nÉtapes de vérification :\n1. Poser la question « quoi ? » ou « qui ? » après le verbe.\n2. Vérifier si la réponse précède le verbe (souvent via le pronom relatif « que » ou un pronom « la, les »).\n3. Accorder le participe passé avec le COD identifié.",
            examplesJson = "J'ai acheté des fleurs. (COD après → invariable)||Les fleurs que j'ai achetées sont parfumées. (COD « les fleurs » avant → féminin pluriel)||La lettre qu'elle a écrite est claire.",
            pitfallsContent = "Ne confondez pas COD et COI ! « Les personnes à qui j'ai parlé » : « à qui » est COI, donc « parlé » reste invariable.",
            estimatedMinutes = 11
        ),
        Lesson(
            id = "b1_conj_01", level = "B1", moduleTitle = "Modes et nuances", chapterOrder = 2,
            theme = "Conjugaison", title = "Le subjonctif présent après les locutions d'obligation",
            summary = "Utiliser le subjonctif à bon escient (il faut que, bien que, afin que).",
            ruleContent = "Le subjonctif exprime une nécessité, un doute, un souhait ou un sentiment. Il est obligatoire après :\n• L'obligation : il faut que, il est nécessaire que.\n• La concession : bien que, quoique.\n• Le but : afin que, pour que.\nFormation régulière : radical de la 3e personne du pluriel au présent + -e, -es, -e, -ions, -iez, -ent. Verbes irréguliers essentiels : être (sois), avoir (aie), aller (aille), faire (fasse), venir (vienne), pouvoir (puisse), savoir (sache).",
            examplesJson = "Il faut que tu viennes à l'heure.||Bien qu'il fasse froid, nous marchons.||Je souhaite que vous réussissiez.",
            pitfallsContent = "Après « après que », la norme académique emploie l'indicatif (Après qu'il est parti). Après « malgré que », préférez toujours « bien que ».",
            estimatedMinutes = 10
        ),
        Lesson(
            id = "b1_comp_01", level = "B1", moduleTitle = "Lecture active", chapterOrder = 3,
            theme = "Compréhension", title = "Identifier les connecteurs logiques d'un texte",
            summary = "Dégager l'articulation argumentative d'un texte grâce aux connecteurs.",
            ruleContent = "Les connecteurs logiques signalent la relation entre deux idées :\n• Cause : car, parce que, puisque, en raison de.\n• Conséquence : donc, par conséquent, ainsi, dès lors.\n• Opposition / Concession : mais, cependant, toutefois, néanmoins, bien que.\n• Addition : de plus, en outre, par ailleurs.",
            examplesJson = "Le délai était court ; néanmoins, l'équipe a livré un travail remarquable.||Puisque la méthode fonctionne, poursuivons nos efforts.",
            pitfallsContent = "Évitez les redondances de connecteurs : « mais cependant » ou « car en effet ».",
            estimatedMinutes = 8
        ),
        // B2 — Intermédiaire avancé
        Lesson(
            id = "b2_conj_01", level = "B2", moduleTitle = "Hypothèse et concordance", chapterOrder = 1,
            theme = "Conjugaison", title = "La concordance des temps dans les phrases hypothétiques (Si)",
            summary = "Construire des hypothèses irréprochables au présent, potentiel et irréel du passé.",
            ruleContent = "La règle d'or : la conjonction hypothétique « si » n'est JAMAIS suivie du conditionnel !\nLes trois schémas canoniques :\n1. Hypothèse probable : Si + présent → futur simple (Si tu révises, tu réussiras).\n2. Hypothèse potentielle / irréel du présent : Si + imparfait → conditionnel présent (Si j'avais le temps, j'irais).\n3. Irréel du passé : Si + plus-que-parfait → conditionnel passé (Si j'avais su, je serais venu).",
            examplesJson = "Si j'avais su, je serais venu.||Si nous disposions d'un jour de plus, nous affinerions ce rapport.||S'il pleut, la séance aura lieu en salle.",
            pitfallsContent = "Ne jamais écrire « Si j'aurais su » ni « Si tu viendrais ». Notez l'élision obligatoire devant « il / ils » : S'il vient (mais « Si elle vient » ne s'élide pas).",
            estimatedMinutes = 10
        ),
        Lesson(
            id = "b2_ecrit_01", level = "B2", moduleTitle = "Rédaction structurée", chapterOrder = 2,
            theme = "Expression écrite", title = "Structurer un texte argumentatif clair et concis",
            summary = "Organiser thèse, arguments, exemples et transitions sans lourdeurs.",
            ruleContent = "Un écrit convaincant repose sur quatre principes :\n1. Une idée directrice par paragraphe.\n2. Des phrases calibrées (15 à 25 mots en moyenne) alternant rythme et clarté.\n3. Des verbes d'action précis à la place des verbes passe-partout (faire, avoir, mettre).\n4. L'élimination des pléonasmes et tics oraux (« au jour d'aujourd'hui », « du coup », « au niveau de »).",
            examplesJson = "Commettre une erreur (au lieu de faire une erreur).||Par conséquent, la réforme s'impose (au lieu de du coup).",
            pitfallsContent = "Évitez l'accumulation de subordonnées relatives (« qui... que... dont... ») dans une même phrase.",
            estimatedMinutes = 9
        ),
        Lesson(
            id = "b2_pro_01", level = "B2", moduleTitle = "Français professionnel", chapterOrder = 3,
            theme = "Français professionnel", title = "Courriels professionnels et formules de politesse justes",
            summary = "Rédiger des échanges professionnels courtois, directs et sans faute.",
            ruleContent = "Dans la correspondance professionnelle francophone :\n• Objet explicite et concis (ex. : Compte rendu — Réunion du 12 mars).\n• Salutation sobre : « Bonjour Madame Martin, » ou « Madame, Monsieur, ».\n• Formules de clôture adaptées : « Cordialement, », « Bien cordialement, », ou dans un cadre formel : « Je vous prie d'agréer, Madame, l'expression de mes salutations distinguées. »",
            examplesJson = "Veuillez trouver ci-joint le rapport consolidé.||Je reste à votre disposition pour tout complément d'information.",
            pitfallsContent = "Attention à « ci-joint » : invariable en début de phrase ou devant un nom sans article (Ci-joint copie de...), variable lorsqu'il est épithète après le nom (Les documents ci-joints).",
            estimatedMinutes = 8
        ),
        // C1 — Avancé
        Lesson(
            id = "c1_synt_01", level = "C1", moduleTitle = "Syntaxe soutenue", chapterOrder = 1,
            theme = "Syntaxe", title = "Pronoms relatifs composés et propositions participiales",
            summary = "Fluidifier la syntaxe grâce à dont, auquel, duquel et aux participiales.",
            ruleContent = "Le choix du pronom relatif dépend de la construction du verbe :\n• « dont » remplace un complément introduit par « de » : Le projet dont je vous parle (parler DE).\n• « auquel / à laquelle / auxquels » remplace un complément introduit par « à » : La règle à laquelle je me réfère.\n• La proposition participiale condense la cause ou le temps : Le travail achevé, l'équipe a célébré sa réussite.",
            examplesJson = "Voici la méthode dont nous avons tiré parti.||La difficulté à laquelle nous faisons face exige de la rigueur.||Les faits étant établis, nous pouvons conclure.",
            pitfallsContent = "Ne jamais combiner « dont » et un possessif redondant (incorrect : « L'auteur dont j'ai lu son livre » → correct : « L'auteur dont j'ai lu le livre »).",
            estimatedMinutes = 12
        ),
        Lesson(
            id = "c1_vocab_01", level = "C1", moduleTitle = "Précision lexicale", chapterOrder = 2,
            theme = "Vocabulaire", title = "Paronymes et nuances sémantiques",
            summary = "Distinguer les mots proches par la forme mais distincts par le sens.",
            ruleContent = "La maîtrise avancée du français passe par la précision lexicale sur les paronymes :\n• Collision (choc) / Collusion (entente secrète).\n• Conjecture (hypothèse) / Conjoncture (situation économique ou sociale).\n• Éminent (remarquable) / Imminent (très proche dans le temps).\n• Effraction (bris) / Infraction (violation d'une règle).\n• Perpétrer (commettre un acte grave) / Perpétuer (faire durer).",
            examplesJson = "La conjoncture actuelle impose une gestion prudente.||Le départ du train est imminent.",
            pitfallsContent = "Attention à « pallier » qui est transitif direct : on écrit « pallier une lacune » (et non « pallier à une lacune »).",
            estimatedMinutes = 10
        ),
        // C2 — Expert
        Lesson(
            id = "c2_style_01", level = "C2", moduleTitle = "Maîtrise stylistique et rhétorique", chapterOrder = 1,
            theme = "Expression écrite", title = "Subtilités des accords complexes et élégance stylistique",
            summary = "Maîtriser les cas d'exception (verbes pronominaux, collectifs, tout/quelque/même).",
            ruleContent = "Les cas de niveau expert exigent une analyse syntaxique fine :\n• Verbes pronominaux : accord avec le COD antéposé (Elles se sont parlé → COI, invariable ; Elles se sont écrit des lettres → COD après, invariable ; Les lettres qu'elles se sont écrites → COD avant, accord).\n• Adverbe « tout » : invariable sauf devant un adjectif féminin commençant par une consonne ou un h aspiré (Elle est tout émue / Elle est toute surprise).\n• Adverbe « même » : invariable quand il signifie « aussi / y compris » (Même les experts vérifient leurs accords).",
            examplesJson = "Elles se sont succédé à la direction.||Elle est tout heureuse mais toute confuse.||La plupart des linguistes s'accordent sur ce point.",
            pitfallsContent = "« Se succéder », « se plaire », « se nuire », « se rire » n'ont jamais de COD : leur participe passé est toujours invariable.",
            estimatedMinutes = 14
        ),
        Lesson(
            id = "c2_oral_01", level = "C2", moduleTitle = "Art oratoire", chapterOrder = 2,
            theme = "Expression orale", title = "Liaisons obligatoires, facultatives et interdites",
            summary = "Parler un français juste et fluide en maîtrisant la prosodie et les liaisons.",
            ruleContent = "Une expression orale soignée distingue trois catégories de liaisons :\n• Obligatoires : déterminant + nom (les_enfants), pronom + verbe (ils_ont, nous_apprenons), adjectif antéposé + nom (un grand_homme).\n• Interdites : après « et » (un pain et // un fruit), devant un h aspiré (des // héros, en // haut), entre un sujet nominal singulier et son verbe (l'étudiant // apprend).\n• Facultatives (registre soutenu) : entre auxiliaire et participe (ils sont_allés), après adverbes courts (très_utile).",
            examplesJson = "Les_étudiants_apprennent le français.||Des // héros et // une histoire passionnante.",
            pitfallsContent = "Évitez le « cuir » ou le « velours » (fausse liaison non étymologique comme « quatre-z-euros »).",
            estimatedMinutes = 9
        )
    )

    fun placementQuestions(): List<PlacementQuestion> = listOf(
        PlacementQuestion(
            id = "pl_1", level = "A1", skill = "Orthographe",
            question = "Quelle phrase est correctement orthographiée ?",
            options = listOf(
                "Les etudiants apprennent le francais.",
                "Les étudiants apprennent le français.",
                "Les étudiants apprend le français.",
                "Les étudiant apprennent le français."
            ),
            correctIndex = 1,
            explanation = "« Étudiants » porte un accent aigu et un « s » du pluriel, le verbe s'accorde au pluriel (« apprennent ») et « français » prend une cédille."
        ),
        PlacementQuestion(
            id = "pl_2", level = "A2", skill = "Grammaire",
            question = "Complétez : « Il travaille dans ___ entreprise depuis deux ans. »",
            options = listOf("sa", "son", "sont", "ses"),
            correctIndex = 1,
            explanation = "Devant un nom féminin singulier commençant par une voyelle (« entreprise »), on utilise « son » par euphonie."
        ),
        PlacementQuestion(
            id = "pl_3", level = "B1", skill = "Conjugaison",
            question = "Complétez : « Il faut que tu ___ à la réunion demain. »",
            options = listOf("viens", "viendra", "viennes", "venais"),
            correctIndex = 2,
            explanation = "La locution « il faut que » exige le subjonctif présent : « que tu viennes »."
        ),
        PlacementQuestion(
            id = "pl_4", level = "B1", skill = "Grammaire",
            question = "Choisissez l'accord exact : « Les fleurs que j'ai ___ sont magnifiques. »",
            options = listOf("acheté", "achetés", "achetée", "achetées"),
            correctIndex = 3,
            explanation = "Le COD « les fleurs » (féminin pluriel), repris par « que », est placé avant l'auxiliaire avoir : accord en « achetées »."
        ),
        PlacementQuestion(
            id = "pl_5", level = "B2", skill = "Conjugaison",
            question = "Quelle formulation respecte la concordance des temps ?",
            options = listOf(
                "Si j'aurais su, je serais venu.",
                "Si j'avais su, je serais venu.",
                "Si j'ai su, je serais venu.",
                "Si j'avais su, je viendrais."
            ),
            correctIndex = 1,
            explanation = "Jamais de conditionnel après « si » : pour l'irréel du passé, on emploie « Si + plus-que-parfait → conditionnel passé »."
        ),
        PlacementQuestion(
            id = "pl_6", level = "C1", skill = "Vocabulaire",
            question = "Quelle phrase utilise le verbe « pallier » de manière normative ?",
            options = listOf(
                "Cette mesure permet de pallier au manque de temps.",
                "Cette mesure permet de pallier le manque de temps.",
                "Cette mesure permet de pallier du manque de temps.",
                "Cette mesure permet de pallier sur le manque de temps."
            ),
            correctIndex = 1,
            explanation = "Le verbe « pallier » est transitif direct : on dit « pallier quelque chose » (sans préposition « à »)."
        ),
        PlacementQuestion(
            id = "pl_7", level = "C2", skill = "Grammaire",
            question = "Quel accord est correct avec le verbe pronominal « se succéder » ?",
            options = listOf(
                "Les directrices se sont succédées pendant dix ans.",
                "Les directrices se sont succédé pendant dix ans.",
                "Les directrices se sont succédés pendant dix ans.",
                "Les directrices se sont succédée pendant dix ans."
            ),
            correctIndex = 1,
            explanation = "On succède À quelqu'un (COI). N'ayant jamais de COD, « succédé » reste toujours invariable."
        )
    )

    fun writingTemplates(): List<WritingTemplate> = listOf(
        WritingTemplate(
            id = "wt_email", category = "Emails", title = "Email professionnel clair",
            objective = "Transmettre une demande ou un suivi avec courtoisie et précision.",
            tips = listOf("Objet explicite", "Une action attendue claire", "Formule de politesse sobre"),
            starterDraft = "Objet : Point d'avancement sur le projet\n\nBonjour Madame,\n\nJe vous écris afin de vous transmettre le bilan de cette semaine. Les principales étapes ont été validées dans les délais prévus.\n\nPourriez-vous me confirmer votre disponibilité jeudi matin pour un échange rapide ?\n\nBien cordialement,\n[Votre nom]"
        ),
        WritingTemplate(
            id = "wt_admin", category = "Lettres administratives", title = "Lettre administrative formelle",
            objective = "Formuler une requête officielle auprès d'un organisme ou d'une mairie.",
            tips = listOf("Mentionner vos références", "Rester factuel et précis", "Clôturer avec une formule protocolaire"),
            starterDraft = "Objet : Demande d'attestation officielle\nRéférence dossier : [Numéro]\n\nMadame, Monsieur,\n\nPar la présente, je sollicite la délivrance d'une attestation relative à mon dossier cité en référence. Vous trouverez ci-joint les pièces justificatives requises.\n\nJe vous prie d'agréer, Madame, Monsieur, l'expression de mes salutations distinguées."
        ),
        WritingTemplate(
            id = "wt_msg_pro", category = "Messages professionnels", title = "Message d'équipe synthétique",
            objective = "Informer rapidement un collègue ou un responsable sur une messagerie interne.",
            tips = listOf("Aller à l'essentiel dès la première ligne", "Éviter les tics familiers comme « du coup »"),
            starterDraft = "Bonjour Camille,\n\nLe document de présentation est prêt pour relecture. J'ai intégré les retours concernant le calendrier et vérifié les chiffres clés.\n\nDis-moi si tu souhaites ajuster un point avant l'envoi de 16 h."
        ),
        WritingTemplate(
            id = "wt_msg_perso", category = "Messages personnels", title = "Message personnel chaleureux",
            objective = "Écrire un message naturel, soigné et sincère à un proche.",
            tips = listOf("Ton authentique et fluide", "Soigner les accords et la ponctuation"),
            starterDraft = "Bonjour Paul,\n\nJ'espère que tu vas bien et que ta semaine s'est déroulée sereinement. Je serais ravi de te retrouver samedi après-midi pour prendre un café et échanger sur nos projets récents.\n\nÀ très bientôt !"
        ),
        WritingTemplate(
            id = "wt_cv", category = "CV", title = "Accroche et expériences de CV",
            objective = "Valoriser ses réalisations avec des verbes d'action précis et mesurables.",
            tips = listOf("Utiliser des verbes d'action à l'infinitif ou au nom", "Quantifier les résultats obtenus"),
            starterDraft = "PROFIL\nChef de projet rigoureux disposant de cinq années d'expérience en coordination d'équipes et amélioration continue.\n\nRÉALISATIONS CLÉS\n• Piloter la refonte complète du parcours client, réduisant les délais de traitement de 30 %.\n• Coordonner une équipe pluridisciplinaire de six collaborateurs dans le respect des budgets."
        ),
        WritingTemplate(
            id = "wt_motivation", category = "Lettres de motivation", title = "Lettre de motivation structurée (Vous / Moi / Nous)",
            objective = "Démontrer l'adéquation entre votre parcours et les besoins de l'entreprise.",
            tips = listOf("Montrer votre connaissance de l'entreprise", "Relier chaque compétence à un résultat concret"),
            starterDraft = "Madame, Monsieur,\n\nVotre engagement en faveur de l'innovation durable retient toute mon attention. Fort d'une solide expérience en gestion de projets numériques, je souhaite mettre mon expertise au service de votre équipe.\n\nAu cours de ma précédente mission, j'ai conçu des outils fiables ayant amélioré la qualité de service au quotidien. Je serais honoré de contribuer à vos ambitions lors d'un prochain entretien.\n\nVeuillez agréer, Madame, Monsieur, mes salutations distinguées."
        ),
        WritingTemplate(
            id = "wt_rapport", category = "Rapports", title = "Synthèse de rapport d'activité",
            objective = "Présenter constats, analyses et recommandations de façon structurée.",
            tips = listOf("Hiérarchiser par constats puis recommandations", "Privilégier des phrases courtes et précises"),
            starterDraft = "SYNTHÈSE EXÉCUTIVE\n\n1. Constat\nAu cours du trimestre écoulé, la fréquentation a progressé de 18 % grâce à l'amélioration de la qualité éditoriale.\n\n2. Recommandations\nIl convient désormais de consolider les délais de réponse et de renforcer la formation interne afin de pérenniser cette dynamique."
        ),
        WritingTemplate(
            id = "wt_linkedin", category = "Publications LinkedIn", title = "Publication professionnelle engageante",
            objective = "Partager un retour d'expérience clair, utile et sans fautes.",
            tips = listOf("Accroche concise", "Aérer par paragraphes courts", "Conclure par une question ouverte"),
            starterDraft = "Trois enseignements clés tirés de notre dernier projet :\n\n1. La clarté écrite fait gagner des heures de coordination.\n2. Relire un message important à froid évite bien des malentendus.\n3. La progression repose sur la régularité quotidienne plutôt que sur l'effort ponctuel.\n\nQuelle habitude vous aide le plus à soigner votre communication professionnelle ?"
        ),
        WritingTemplate(
            id = "wt_presentation", category = "Présentations", title = "Introduction d'une présentation orale",
            objective = "Capter l'attention de l'auditoire et annoncer un plan limpide.",
            tips = listOf("Phrases rythmées faciles à dire à voix haute", "Annoncer clairement les 3 parties"),
            starterDraft = "Bonjour à toutes et à tous,\n\nJe vous remercie de votre présence. Aujourd'hui, nous allons examiner comment améliorer durablement la qualité de nos écrits professionnels.\n\nMon intervention s'articule en trois temps : d'abord un état des lieux, ensuite les méthodes concrètes d'amélioration, et enfin un plan d'action immédiatement applicable."
        ),
        WritingTemplate(
            id = "wt_argument", category = "Textes argumentatifs", title = "Paragraphe argumentatif nuancé",
            objective = "Défendre une thèse avec connecteurs logiques, nuances et exemples.",
            tips = listOf("Articuler concession et réfutation", "Soigner les connecteurs (certes, toutefois, par conséquent)"),
            starterDraft = "Certes, les outils numériques accélèrent considérablement la rédaction quotidienne. Toutefois, cette rapidité ne saurait remplacer la maîtrise personnelle de la langue. En effet, comprendre les règles grammaticales permet d'exprimer des nuances précises et de renforcer la crédibilité de son propos. Par conséquent, l'apprentissage régulier du français demeure un atout essentiel."
        )
    )

    fun speakingDrills(): List<SpeakingDrill> = listOf(
        SpeakingDrill(
            id = "sp_1", level = "A1", focus = "Liaison obligatoire & voyelles nasales",
            sentence = "Les étudiants apprennent le français avec enthousiasme.",
            phoneticGuide = "[le.z‿e.ty.djɑ̃.z‿a.pʁɛn lə fʁɑ̃.sɛ a.vɛk ɑ̃.tu.zjasm]",
            coachingTip = "Faites clairement la liaison [z] dans « Les‿étudiants » et distinguez bien [ɑ̃] (an/en) de [ɛ̃] (in)."
        ),
        SpeakingDrill(
            id = "sp_2", level = "A2", focus = "Son [y] vs [u] et rythme",
            sentence = "Les enfants jouent dans la cour pendant la récréation.",
            phoneticGuide = "[le.z‿ɑ̃.fɑ̃ ʒu dɑ̃ la kuʁ pɑ̃.dɑ̃ la ʁe.kʁe.a.sjɔ̃]",
            coachingTip = "Arrondissez bien les lèvres sur « jouent » et « cour » ([u]), et marquez une légère pause après « cour »."
        ),
        SpeakingDrill(
            id = "sp_3", level = "B1", focus = "Fluidité du subjonctif",
            sentence = "Il faut que tu viennes avant que la séance ne commence.",
            phoneticGuide = "[il fo kə ty vjɛn a.vɑ̃ kə la se.ɑ̃s nə kɔ.mɑ̃s]",
            coachingTip = "Enchaînez « il faut que tu viennes » d'un seul souffle sans casser le rythme avant « viennes »."
        ),
        SpeakingDrill(
            id = "sp_4", level = "B2", focus = "Conditionnel & intonation montante/descendante",
            sentence = "Si j'avais su, je serais venu présenter notre stratégie.",
            phoneticGuide = "[si ʒa.vɛ sy ʒə sə.ʁɛ və.ny pʁe.zɑ̃.te nɔtʁ stʁa.te.ʒi]",
            coachingTip = "Montez légèrement l'intonation sur « Si j'avais su » puis descendez posément jusqu'à « stratégie »."
        ),
        SpeakingDrill(
            id = "sp_5", level = "C1", focus = "Articulation soutenue & consonnes finales",
            sentence = "Les fleurs que j'ai achetées embellissent remarquablement la salle.",
            phoneticGuide = "[le flœʁ kə ʒe aʃ.te ɑ̃.bɛ.lis ʁə.maʁ.ka.blə.mɑ̃ la sal]",
            coachingTip = "Articulez distinctement toutes les syllabes de « remarquablement » sans précipiter le débit."
        )
    )
}
