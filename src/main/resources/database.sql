-- ================================================================
-- CLINIQUE SYNC — Base de donnees v4 (PROPRE)
-- Tables utilisees par l application :
--   utilisateurs, patients, medecins, rendez_vous,
--   prescriptions, factures, notifications
-- Tables supprimees (inutilisees) :
--   analyses, consultations, file_attente, messages, logs_actions
-- ================================================================

CREATE DATABASE IF NOT EXISTS doctovite
  CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE doctovite;

SET FOREIGN_KEY_CHECKS = 0;
DROP TABLE IF EXISTS notifications;
DROP TABLE IF EXISTS prescriptions;
DROP TABLE IF EXISTS factures;
DROP TABLE IF EXISTS rendez_vous;
DROP TABLE IF EXISTS medecins;
DROP TABLE IF EXISTS patients;
DROP TABLE IF EXISTS utilisateurs;
SET FOREIGN_KEY_CHECKS = 1;

-- ================================================================
-- TABLE 1 : utilisateurs
-- Stocke tous les comptes (Admin, Medecin, Secretaire)
-- ================================================================
CREATE TABLE utilisateurs (
    identifiant   INT AUTO_INCREMENT PRIMARY KEY,
    cin           VARCHAR(20)  UNIQUE NOT NULL,
    nom_famille   VARCHAR(100) NOT NULL,
    prenom        VARCHAR(100) NOT NULL,
    mot_passe     VARCHAR(255) NOT NULL,
    role          ENUM('ADMINISTRATEUR','MEDECIN','SECRETAIRE') NOT NULL,
    adresse_email VARCHAR(150),
    num_telephone VARCHAR(20),
    actif         TINYINT(1) DEFAULT 1
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ================================================================
-- TABLE 2 : patients
-- ================================================================
CREATE TABLE patients (
    identifiant    INT AUTO_INCREMENT PRIMARY KEY,
    numero_dossier VARCHAR(20) UNIQUE NOT NULL,
    cin            VARCHAR(20) UNIQUE,
    nom_famille    VARCHAR(100) NOT NULL,
    prenom         VARCHAR(100) NOT NULL,
    date_naissance DATE,
    telephone      VARCHAR(20),
    adresse_email  VARCHAR(150),
    adresse        VARCHAR(255),
    groupe_sanguin VARCHAR(5),
    antecedents    TEXT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ================================================================
-- TABLE 3 : medecins
-- Profil medical lie a un compte utilisateur
-- ================================================================
CREATE TABLE medecins (
    identifiant     INT AUTO_INCREMENT PRIMARY KEY,
    utilisateur_id  INT NOT NULL,
    specialite      VARCHAR(120) NOT NULL,
    heure_debut     TIME DEFAULT '08:00:00',
    heure_fin       TIME DEFAULT '17:00:00',
    statut_presence ENUM('EN_LIGNE','HORS_LIGNE','EN_CONSULTATION') DEFAULT 'HORS_LIGNE',
    FOREIGN KEY (utilisateur_id) REFERENCES utilisateurs(identifiant) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ================================================================
-- TABLE 4 : rendez_vous
-- ================================================================
CREATE TABLE rendez_vous (
    identifiant  INT AUTO_INCREMENT PRIMARY KEY,
    patient_id   INT NOT NULL,
    medecin_id   INT NOT NULL,
    date_rdv     DATE NOT NULL,
    heure_rdv    TIME NOT NULL,
    motif        VARCHAR(255),
    statut       ENUM('PLANIFIE','CONFIRME','ANNULE','TERMINE','URGENCE') DEFAULT 'PLANIFIE',
    cree_par     INT,
    FOREIGN KEY (patient_id) REFERENCES patients(identifiant)    ON DELETE CASCADE,
    FOREIGN KEY (medecin_id) REFERENCES medecins(identifiant)    ON DELETE CASCADE,
    FOREIGN KEY (cree_par)   REFERENCES utilisateurs(identifiant) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ================================================================
-- TABLE 5 : prescriptions (ordonnances)
-- NB : pas de FK vers consultations (table supprimee)
-- ================================================================
CREATE TABLE prescriptions (
    identifiant      INT AUTO_INCREMENT PRIMARY KEY,
    patient_id       INT NOT NULL,
    medecin_id       INT NOT NULL,
    medicaments      TEXT NOT NULL,
    posologie        TEXT,
    duree_traitement VARCHAR(100),
    observations     TEXT,
    date_emission    DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (patient_id) REFERENCES patients(identifiant) ON DELETE CASCADE,
    FOREIGN KEY (medecin_id) REFERENCES medecins(identifiant) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ================================================================
-- TABLE 6 : factures
-- ================================================================
CREATE TABLE factures (
    identifiant      INT AUTO_INCREMENT PRIMARY KEY,
    patient_id       INT NOT NULL,
    rdv_id           INT,
    numero_facture   VARCHAR(30) UNIQUE NOT NULL,
    montant_total    DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    montant_paye     DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    mode_paiement    ENUM('ESPECES','CARTE','VIREMENT','ASSURANCE') DEFAULT 'ESPECES',
    statut_paiement  ENUM('EN_ATTENTE','PARTIEL','SOLDE') DEFAULT 'EN_ATTENTE',
    description      TEXT,
    cree_par         INT,
    date_creation    DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (patient_id) REFERENCES patients(identifiant)    ON DELETE CASCADE,
    FOREIGN KEY (rdv_id)     REFERENCES rendez_vous(identifiant) ON DELETE SET NULL,
    FOREIGN KEY (cree_par)   REFERENCES utilisateurs(identifiant) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ================================================================
-- TABLE 7 : notifications
-- ================================================================
CREATE TABLE notifications (
    identifiant      INT AUTO_INCREMENT PRIMARY KEY,
    destinataire_id  INT NOT NULL,
    expediteur_id    INT,
    contenu          TEXT NOT NULL,
    type_notif       VARCHAR(30) DEFAULT 'INFO',
    est_lu           TINYINT(1) DEFAULT 0,
    date_envoi       DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (destinataire_id) REFERENCES utilisateurs(identifiant) ON DELETE CASCADE,
    FOREIGN KEY (expediteur_id)   REFERENCES utilisateurs(identifiant) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ================================================================
-- DONNEES : 1 Admin
-- ================================================================
INSERT INTO utilisateurs (cin,nom_famille,prenom,mot_passe,role,adresse_email,num_telephone) VALUES
('ADM001','Bensouda','Rachid','admin123','ADMINISTRATEUR','r.bensouda@doctovite.ma','0661001001');

-- ================================================================
-- DONNEES : 20 Medecins
-- ================================================================
INSERT INTO utilisateurs (cin,nom_famille,prenom,mot_passe,role,adresse_email,num_telephone) VALUES
('MED001','Idrissi','Karim','med123','MEDECIN','k.idrissi@doctovite.ma','0661001002'),
('MED002','Rahali','Fatima','med123','MEDECIN','f.rahali@doctovite.ma','0661001003'),
('MED003','Boukhari','Omar','med123','MEDECIN','o.boukhari@doctovite.ma','0661001004'),
('MED004','Tazi','Samira','med123','MEDECIN','s.tazi@doctovite.ma','0661001005'),
('MED005','Alaoui','Youssef','med123','MEDECIN','y.alaoui@doctovite.ma','0661001006'),
('MED006','Bennis','Nadia','med123','MEDECIN','n.bennis@doctovite.ma','0661001007'),
('MED007','Chraibi','Rachid','med123','MEDECIN','r.chraibi@doctovite.ma','0661001008'),
('MED008','Fassi','Leila','med123','MEDECIN','l.fassi@doctovite.ma','0661001009'),
('MED009','Guessous','Hassan','med123','MEDECIN','h.guessous@doctovite.ma','0661001010'),
('MED010','Hajji','Zineb','med123','MEDECIN','z.hajji@doctovite.ma','0661001011'),
('MED011','Kettani','Mehdi','med123','MEDECIN','m.kettani@doctovite.ma','0661001012'),
('MED012','Lahlou','Amina','med123','MEDECIN','a.lahlou@doctovite.ma','0661001013'),
('MED013','Mansouri','Tariq','med123','MEDECIN','t.mansouri@doctovite.ma','0661001014'),
('MED014','Naciri','Houda','med123','MEDECIN','h.naciri@doctovite.ma','0661001015'),
('MED015','Ouali','Bilal','med123','MEDECIN','b.ouali@doctovite.ma','0661001016'),
('MED016','Qadiri','Meryem','med123','MEDECIN','m.qadiri@doctovite.ma','0661001017'),
('MED017','Rhazali','Amine','med123','MEDECIN','a.rhazali@doctovite.ma','0661001018'),
('MED018','Senhaji','Dounia','med123','MEDECIN','d.senhaji@doctovite.ma','0661001019'),
('MED019','Tahiri','Khalid','med123','MEDECIN','k.tahiri@doctovite.ma','0661001020'),
('MED020','Zirari','Sanaa','med123','MEDECIN','s.zirari@doctovite.ma','0661001021');

-- ================================================================
-- DONNEES : 4 Secretaires
-- ================================================================
INSERT INTO utilisateurs (cin,nom_famille,prenom,mot_passe,role,adresse_email,num_telephone) VALUES
('SEC001','Ziani','Nadia','sec123','SECRETAIRE','n.ziani@doctovite.ma','0661001022'),
('SEC002','Berrada','Hanane','sec123','SECRETAIRE','h.berrada@doctovite.ma','0661001023'),
('SEC003','Cherkaoui','Imane','sec123','SECRETAIRE','i.cherkaoui@doctovite.ma','0661001024'),
('SEC004','Moussaoui','Salma','sec123','SECRETAIRE','s.moussaoui@doctovite.ma','0661001025');

-- ================================================================
-- DONNEES : Profils medecins (20 specialites)
-- utilisateur_id 2 a 21 = les 20 medecins ci-dessus
-- ================================================================
INSERT INTO medecins (utilisateur_id,specialite,heure_debut,heure_fin) VALUES
(2, 'Cardiologie',               '08:00:00','16:00:00'),
(3, 'Dermatologie',              '09:00:00','17:00:00'),
(4, 'Pediatrie',                 '08:00:00','15:00:00'),
(5, 'Neurologie',                '09:00:00','17:00:00'),
(6, 'Ophtalmologie',             '08:00:00','16:00:00'),
(7, 'Gynecologie-Obstetrique',   '09:00:00','17:00:00'),
(8, 'Orthopedie',                '08:00:00','16:00:00'),
(9, 'Psychiatrie',               '10:00:00','18:00:00'),
(10,'Endocrinologie',            '08:00:00','16:00:00'),
(11,'Gastro-enterologie',        '09:00:00','17:00:00'),
(12,'Pneumologie',               '08:00:00','16:00:00'),
(13,'Urologie',                  '09:00:00','17:00:00'),
(14,'Rhumatologie',              '08:00:00','16:00:00'),
(15,'Nephrologie',               '09:00:00','17:00:00'),
(16,'Oncologie',                 '08:00:00','16:00:00'),
(17,'Chirurgie generale',        '09:00:00','17:00:00'),
(18,'Medecine interne',          '08:00:00','16:00:00'),
(19,'Radiologie',                '09:00:00','17:00:00'),
(20,'ORL',                       '08:00:00','16:00:00'),
(21,'Medecine urgence',          '00:00:00','23:59:00');

-- ================================================================
-- DONNEES : 30 Patients
-- ================================================================
INSERT INTO patients (numero_dossier,cin,nom_famille,prenom,date_naissance,telephone,adresse,groupe_sanguin,antecedents) VALUES
('DOS-001','P001','Mansouri','Hassan',     '1985-03-15','0611100001','Rue Al Farabi, Casablanca',   'A+', 'Diabete type 2, HTA'),
('DOS-002','P002','Chraibi', 'Sara',       '1992-07-22','0611100002','Bd Mohammed V, Rabat',        'O+', 'Hypertension arterielle'),
('DOS-003','P003','Tazi',    'Youssef',    '1978-11-05','0611100003','Av Hassan II, Fes',           'B-', 'Asthme bronchique'),
('DOS-004','P004','Bennis',  'Laila',      '2001-02-18','0611100004','Rue Sebou, Meknes',           'AB+','Allergie penicilline'),
('DOS-005','P005','Hajji',   'Mehdi',      '1965-09-30','0611100005','Quartier Admin, Agadir',      'O-', 'Antecedent cardiaque, arythmie'),
('DOS-006','P006','Alaoui',  'Fatima',     '1990-05-12','0611100006','Rue Zerktouni, Marrakech',    'A-', 'RAS'),
('DOS-007','P007','Berrada', 'Karim',      '1975-08-20','0611100007','Av FAR, Tanger',              'B+', 'Cholesterol eleve'),
('DOS-008','P008','Kettani', 'Amina',      '2005-12-01','0611100008','Quartier Habous, Casablanca', 'AB-','RAS'),
('DOS-009','P009','Guessous','Tariq',      '1958-04-17','0611100009','Medina, Fes',                 'O+', 'Insuffisance renale chronique'),
('DOS-010','P010','Rahali',  'Zineb',      '1995-06-28','0611100010','Av Allal Fassi, Rabat',       'A+', 'Migraine chronique'),
('DOS-011','P011','Fassi',   'Mourad',     '1980-01-10','0611100011','Rue Ibn Battouta, Casablanca','B+', 'RAS'),
('DOS-012','P012','Idrissi', 'Khadija',    '1972-09-03','0611100012','Bd Moulay Ismail, Meknes',   'O+', 'Diabete type 1'),
('DOS-013','P013','Lahlou',  'Anas',       '1999-04-25','0611100013','Rue Oqba, Rabat',             'A-', 'RAS'),
('DOS-014','P014','Naciri',  'Safae',      '1988-07-14','0611100014','Av Mohammed VI, Marrakech',   'AB+','Thyroide - Hashimoto'),
('DOS-015','P015','Qadiri',  'Hicham',     '1963-12-30','0611100015','Rue Tanger, Casablanca',      'O+', 'Coronaropathie, stent 2019'),
('DOS-016','P016','Senhaji', 'Nour',       '2010-03-08','0611100016','Quartier Agdal, Rabat',       'B-', 'Asthme allergique'),
('DOS-017','P017','Ouali',   'Rachida',    '1955-06-19','0611100017','Medina, Marrakech',           'A+', 'Arthrose, HTA'),
('DOS-018','P018','Bensouda','Yassine',    '1993-11-22','0611100018','Rue Bir Anzarane, Casablanca','O+', 'RAS'),
('DOS-019','P019','Rhazali', 'Fatna',      '1969-08-05','0611100019','Av Hassan I, Fes',            'A-', 'Cancer sein'),
('DOS-020','P020','Tahiri',  'Driss',      '2003-02-14','0611100020','Rue Imam Malik, Tanger',      'O-', 'RAS'),
('DOS-021','P021','Zirari',  'Meryem',     '1987-05-30','0611100021','Bd Zerktouni, Casablanca',    'B+', 'SOPK'),
('DOS-022','P022','Bakkali', 'Khalid',     '1971-10-12','0611100022','Rue des FAR, Agadir',         'A+', 'HTA severe, IC'),
('DOS-023','P023','Mekouar', 'Houda',      '1996-03-27','0611100023','Av Moulay Abdallah, Rabat',   'AB-','RAS'),
('DOS-024','P024','Squalli', 'Adil',       '1982-07-09','0611100024','Rue Lebsir, Casablanca',      'O+', 'Ulcere gastrique'),
('DOS-025','P025','Amrani',  'Soukaina',   '1979-01-18','0611100025','Quartier Hay Riad, Rabat',    'A+', 'Epilepsie'),
('DOS-026','P026','Filali',  'Noureddine', '1966-04-23','0611100026','Rue Al Massira, Fes',         'B+', 'BPCO, fumeur'),
('DOS-027','P027','Kadiri',  'Malak',      '2000-08-16','0611100027','Av Prince My Abdallah, Tanger','O+','RAS'),
('DOS-028','P028','Berrechid','Ayoub',     '1990-12-05','0611100028','Rue Lalla Yto, Casablanca',   'A-', 'Hernie discale L4-L5'),
('DOS-029','P029','Wahbi',   'Naima',      '1975-09-22','0611100029','Av Nations Unies, Rabat',     'AB+','Hypothyroidie'),
('DOS-030','P030','Tahani',  'Ibrahim',    '1958-03-14','0611100030','Rue Ain Sebaa, Casablanca',   'O+', 'Cirrhose VHC');

-- ================================================================
-- DONNEES : Rendez-vous
-- medecin_id = identifiant dans la table medecins (1 a 20)
-- patient_id = identifiant dans la table patients  (1 a 30)
-- cree_par   = identifiant secretaire (22=Nadia, 23=Hanane)
-- ================================================================
INSERT INTO rendez_vous (patient_id,medecin_id,date_rdv,heure_rdv,motif,statut,cree_par) VALUES
(1,  1, CURDATE(), '09:00:00', 'Suivi cardiologique mensuel',       'CONFIRME', 22),
(2,  2, CURDATE(), '09:30:00', 'Controle acne',                     'PLANIFIE', 22),
(3,  1, CURDATE(), '10:00:00', 'Resultats ECG',                     'PLANIFIE', 22),
(4,  3, CURDATE(), '10:30:00', 'Vaccination rappel',                'CONFIRME', 22),
(5,  1, CURDATE(), '11:00:00', 'Douleurs thoraciques urgentes',     'URGENCE',  22),
(6,  4, CURDATE(), '11:30:00', 'Cephalees persistantes',            'PLANIFIE', 22),
(7,  5, CURDATE(), '14:00:00', 'Examen ophtalmologique',            'CONFIRME', 23),
(8,  6, CURDATE(), '14:30:00', 'Suivi grossesse',                   'PLANIFIE', 23),
(9,  1, CURDATE(), '15:00:00', 'Suivi post-stent',                  'CONFIRME', 23),
(10, 3, CURDATE(), '15:30:00', 'Otite - fievre',                    'PLANIFIE', 23),
(11, 7, CURDATE(), '16:00:00', 'Douleur genou gauche',              'PLANIFIE', 22),
(12, 9, CURDATE(), '16:30:00', 'Bilan thyroidien',                  'CONFIRME', 22),
(13, 1, DATE_ADD(CURDATE(), INTERVAL 1 DAY), '09:00:00', 'Palpitations',           'PLANIFIE', 22),
(14, 6, DATE_ADD(CURDATE(), INTERVAL 1 DAY), '09:30:00', 'Suivi grossesse 7e mois','PLANIFIE', 22),
(15, 1, DATE_ADD(CURDATE(), INTERVAL 1 DAY), '10:00:00', 'Coronaropathie bilan',   'PLANIFIE', 22),
(16, 3, DATE_ADD(CURDATE(), INTERVAL 1 DAY), '10:30:00', 'Asthme enfant',          'PLANIFIE', 23),
(17, 7, DATE_ADD(CURDATE(), INTERVAL 1 DAY), '11:00:00', 'Arthrose revision',      'PLANIFIE', 23),
(18, 4, DATE_ADD(CURDATE(), INTERVAL 1 DAY), '14:00:00', 'Maux de tete',           'PLANIFIE', 23),
(19,15, DATE_ADD(CURDATE(), INTERVAL 1 DAY), '14:30:00', 'Suivi oncologie',        'CONFIRME', 22),
(20,20, DATE_ADD(CURDATE(), INTERVAL 1 DAY), '15:00:00', 'Otite externe',          'PLANIFIE', 22),
(21, 6, DATE_ADD(CURDATE(), INTERVAL 2 DAY), '09:00:00', 'SOPK suivi',             'PLANIFIE', 22),
(22, 1, DATE_ADD(CURDATE(), INTERVAL 2 DAY), '09:30:00', 'IC decompensation',      'URGENCE',  23),
(23, 2, DATE_ADD(CURDATE(), INTERVAL 2 DAY), '10:00:00', 'Dermoscopie',            'PLANIFIE', 23),
(24,10, DATE_ADD(CURDATE(), INTERVAL 2 DAY), '10:30:00', 'Douleurs epigastriques', 'PLANIFIE', 22),
(25, 4, DATE_ADD(CURDATE(), INTERVAL 2 DAY), '11:00:00', 'Epilepsie ajustement',   'PLANIFIE', 22),
(26,11, DATE_ADD(CURDATE(), INTERVAL 3 DAY), '09:00:00', 'BPCO spirometrie',       'PLANIFIE', 22),
(27,13, DATE_ADD(CURDATE(), INTERVAL 3 DAY), '09:30:00', 'Calcul renal',           'PLANIFIE', 23),
(28, 7, DATE_ADD(CURDATE(), INTERVAL 3 DAY), '10:00:00', 'Hernie discale',         'PLANIFIE', 23),
(29,17, DATE_ADD(CURDATE(), INTERVAL 3 DAY), '10:30:00', 'Hypothyroidie TSH',      'PLANIFIE', 22),
(30,10, DATE_ADD(CURDATE(), INTERVAL 3 DAY), '11:00:00', 'Cirrhose bilan',         'CONFIRME', 22);

-- ================================================================
-- DONNEES : Prescriptions (ordonnances)
-- patient_id et medecin_id correspondent aux IDs ci-dessus
-- ================================================================
INSERT INTO prescriptions (patient_id,medecin_id,medicaments,posologie,duree_traitement,observations) VALUES
(1,  1, 'Amlodipine 5mg, Ramipril 10mg, Atorvastatin 20mg',
        '1 cp matin, 1 cp soir, 1 cp coucher', '3 mois', 'Surveiller creatinine. Regime sans sel.'),
(2,  2, 'Adapalene 0.1% gel, Clindamycine 1%',
        'Application soir, application matin', '2 mois', 'Eviter soleil. SPF50+ obligatoire.'),
(5,  1, 'Aspirine 100mg, Bisoprolol 5mg, Furosemide 40mg',
        '1 cp matin, 1/2 cp matin, 1 cp matin', 'Continu', 'Surveillance cardiaque. ECG hebdomadaire.'),
(3,  3, 'Salbutamol spray, Beclometasone 250mcg',
        '2 bouffees si besoin, 2 bouffees matin/soir', 'Continu', 'Eviter allergenes.'),
(9,  1, 'Losartan 50mg, Furosemide 40mg, Calcium 500mg',
        '1 cp matin, 1 cp matin, 1 cp midi', 'Continu', 'DFG surveiller. Consultation nephro mensuelle.'),
(7,  1, 'Rosuvastatine 20mg, Acide acetylsalicylique 100mg',
        '1 cp soir, 1 cp matin', 'Continu', 'Bilan lipidique dans 3 mois. Regime pauvre en graisses.'),
(15, 1, 'Clopidogrel 75mg, Aspirine 100mg, Bisoprolol 5mg',
        '1 cp matin, 1 cp matin, 1/2 cp matin', 'Continu', 'Double anti-agregation post-stent.'),
(22, 1, 'Furosemide 80mg, Spironolactone 25mg, Ramipril 5mg',
        '1 cp matin, 1 cp soir, 1 cp matin', 'Continu', 'Electrolytes hebdomadaire. Peser chaque jour.'),
(25, 4, 'Levetiracetam 500mg, Vitamine B6 40mg',
        '1 cp matin + 1 cp soir, 1 cp matin', 'Continu', 'EEG dans 3 mois. Conduite interdite.'),
(28, 7, 'Diclofenac 75mg, Esomeprazole 20mg, Methocarbamol 1500mg',
        '1 cp soir repas, 1 cp matin, 1 cp matin/soir', '1 mois', 'Hernie L4-L5. Repos relatif.'),
(17, 7, 'Paracetamol 1g, Ibuprofene 400mg, Pantoprazole 20mg',
        '1g toutes 6h, 400mg matin/soir repas, 1 cp matin', '3 mois', 'Arthrose genou. Kinesitherapie 10 seances.'),
(26,11, 'Tiotropium 18mcg, Salbutamol 100mcg, N-Acetylcysteine 600mg',
        '1 inhalation matin, 2 bouffees si dyspnee, 1 sachet matin', 'Continu', 'BPCO GOLD 2. Sevrage tabac urgent.'),
(24,10, 'Omeprazole 20mg, Amoxicilline 1g, Claritromycine 500mg',
        '1 cp matin/soir, 1 cp matin/soir, 1 cp matin/soir', '7 jours', 'Ulcere H. pylori. Endoscopie controle 6 semaines.'),
(19,15, 'Anastrozole 1mg, Calcium 1000mg + Vitamine D 800UI',
        '1 cp matin, 1 cp soir', '5 ans', 'Cancer sein RE+. Osteodensiometrie annuelle.'),
(29,17, 'Levothyroxine 75mcg, Calcium carbonate 1g, Vitamine D3 800UI',
        '1 cp matin a jeun, 1 cp soir, 1 cp midi', 'Vie entiere', 'Hypothyroidie. TSH cible 0.5-2.5 mUI/L.');

-- ================================================================
-- DONNEES : Factures
-- ================================================================
INSERT INTO factures (patient_id,rdv_id,numero_facture,montant_total,montant_paye,mode_paiement,statut_paiement,description,cree_par) VALUES
(1, 1, 'FAC-2025-001', 350.00, 350.00, 'CARTE',    'SOLDE',     'Consultation cardio + ECG + Bilan lipidique', 22),
(2, 2, 'FAC-2025-002', 200.00, 200.00, 'ESPECES',  'SOLDE',     'Consultation dermato + Dermoscopie',         23),
(5, 5, 'FAC-2025-003',1200.00, 600.00, 'ASSURANCE','PARTIEL',   'Consultation urgente + examens',             22),
(4, 4, 'FAC-2025-004', 150.00, 150.00, 'ESPECES',  'SOLDE',     'Consultation pediatrique + Vaccination',     23),
(9,NULL,'FAC-2025-005', 280.00,   0.00,'ESPECES',  'EN_ATTENTE','Bilan renal + Consultation nephro',          22);

-- ================================================================
-- DONNEES : Notifications initiales
-- ================================================================
INSERT INTO notifications (destinataire_id,expediteur_id,contenu,type_notif,est_lu) VALUES
(2, 22,'Nouveau RDV : Hassan Mansouri - Suivi cardiologique','RDV',    0),
(3, 22,'Nouveau RDV : Laila Bennis - Vaccination DTP',        'RDV',    1),
(4, 23,'Nouveau RDV : Fatima Alaoui - Neurologie',            'RDV',    0),
(2, 22,'URGENCE : Mehdi Hajji - douleurs thoraciques',        'URGENCE',0);

-- ================================================================
-- VERIFICATION FINALE
-- ================================================================
SELECT 'BASE CLINIQUE SYNC CREEE AVEC SUCCES' AS resultat;
SELECT CONCAT('Utilisateurs : ', COUNT(*)) AS info FROM utilisateurs;
SELECT CONCAT('Patients : ',     COUNT(*)) AS info FROM patients;
SELECT CONCAT('Medecins : ',     COUNT(*)) AS info FROM medecins;
SELECT CONCAT('Rendez-vous : ',  COUNT(*)) AS info FROM rendez_vous;
SELECT CONCAT('Ordonnances : ',  COUNT(*)) AS info FROM prescriptions;
SELECT CONCAT('Factures : ',     COUNT(*)) AS info FROM factures;
