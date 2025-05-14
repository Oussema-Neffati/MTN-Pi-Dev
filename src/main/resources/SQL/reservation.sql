-- phpMyAdmin SQL Dump
-- version 5.2.1
-- https://www.phpmyadmin.net/
--
-- Host: 127.0.0.1:3306
-- Generation Time: May 14, 2025 at 05:21 PM
-- Server version: 9.1.0
-- PHP Version: 8.3.14

SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
START TRANSACTION;
SET time_zone = "+00:00";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;

--
-- Database: `base_commune`
--

-- --------------------------------------------------------

--
-- Table structure for table `demande`
--

DROP TABLE IF EXISTS `demande`;
CREATE TABLE IF NOT EXISTS `demande` (
                                         `id` int NOT NULL AUTO_INCREMENT,
                                         `id_user` int NOT NULL,
                                         `nom` varchar(100) NOT NULL,
    `Adresse` varchar(255) DEFAULT NULL,
    `type` varchar(50) NOT NULL,
    `price` decimal(10,2) DEFAULT '0.00',
    PRIMARY KEY (`id`)
    ) ENGINE=MyISAM DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- --------------------------------------------------------

--
-- Table structure for table `document`
--

DROP TABLE IF EXISTS `document`;
CREATE TABLE IF NOT EXISTS `document` (
                                          `id_doc` int NOT NULL AUTO_INCREMENT,
                                          `type_doc` varchar(200) NOT NULL,
    `statut_doc` varchar(200) NOT NULL,
    `date_emission_doc` varchar(50) DEFAULT NULL,
    `date_expiration_doc` varchar(50) DEFAULT NULL,
    `archive` tinyint(1) DEFAULT '1',
    `nb_req` int DEFAULT '0',
    PRIMARY KEY (`id_doc`)
    ) ENGINE=MyISAM DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- --------------------------------------------------------

--
-- Table structure for table `documentrequest`
--

DROP TABLE IF EXISTS `documentrequest`;
CREATE TABLE IF NOT EXISTS `documentrequest` (
                                                 `id_d_doc` int NOT NULL AUTO_INCREMENT,
                                                 `type_d_doc` varchar(200) NOT NULL,
    `description_d_doc` text,
    `statut_d_doc` enum('Pending','Approved','Rejected','Completed') NOT NULL DEFAULT 'Pending',
    `date_d_doc` date DEFAULT NULL,
    `date_traitement_d_doc` datetime DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id_d_doc`)
    ) ENGINE=MyISAM DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- --------------------------------------------------------

--
-- Table structure for table `projets`
--

DROP TABLE IF EXISTS `projets`;
CREATE TABLE IF NOT EXISTS `projets` (
                                         `id` int NOT NULL AUTO_INCREMENT,
                                         `nom` varchar(255) NOT NULL,
    `categorie` varchar(50) NOT NULL,
    `date_debut` date NOT NULL,
    `date_fin` date NOT NULL,
    PRIMARY KEY (`id`)
    ) ENGINE=MyISAM AUTO_INCREMENT=16 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

--
-- Dumping data for table `projets`
--

INSERT INTO `projets` (`id`, `nom`, `categorie`, `date_debut`, `date_fin`) VALUES
                                                                               (1, 'Projet Alpha', 'En cours', '2025-01-01', '2025-06-30'),
                                                                               (2, 'Projet Beta', 'Terminé', '2024-07-01', '2024-12-31'),
                                                                               (15, 'nouha', 'En cours', '2025-05-01', '2025-05-30'),
                                                                               (4, 'Nefzi007', 'En cours', '2025-05-01', '2025-05-30'),
                                                                               (5, 'mahdi', 'En cours', '2025-05-01', '2025-05-30'),
                                                                               (6, 'firas', 'Terminé', '2025-05-01', '2025-05-31'),
                                                                               (7, 'aziz', 'Terminé', '2025-05-01', '2025-05-31'),
                                                                               (8, 'gharbi', 'En cours', '2025-05-01', '2025-05-31'),
                                                                               (9, 'oussema', 'Terminé', '2025-05-01', '2025-05-31'),
                                                                               (10, 'achref', 'Terminé', '2025-05-01', '2025-05-02'),
                                                                               (11, 'amira', 'Terminé', '2025-05-01', '2025-05-30'),
                                                                               (12, 'safa', 'Terminé', '2025-05-01', '2025-05-31'),
                                                                               (13, 'salsso', 'Terminé', '2025-05-01', '2025-05-31'),
                                                                               (14, 'Wissem', 'En cours', '2025-05-01', '2025-05-31');

-- --------------------------------------------------------

--
-- Table structure for table `reservation`
--

DROP TABLE IF EXISTS `reservation`;
CREATE TABLE IF NOT EXISTS `reservation` (
                                             `idRes` int NOT NULL AUTO_INCREMENT,
                                             `dateReservation` date NOT NULL,
                                             `heureDebut` varchar(8) NOT NULL,
    `heureFin` varchar(8) NOT NULL,
    `status` varchar(20) NOT NULL,
    `nombreParticipants` int NOT NULL,
    `motif` varchar(255) DEFAULT NULL,
    `idUtilisateur` int NOT NULL,
    `idRessource` int NOT NULL,
    PRIMARY KEY (`idRes`),
    KEY `idUtilisateur` (`idUtilisateur`),
    KEY `idRessource` (`idRessource`)
    ) ENGINE=MyISAM AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

--
-- Dumping data for table `reservation`
--

INSERT INTO `reservation` (`idRes`, `dateReservation`, `heureDebut`, `heureFin`, `status`, `nombreParticipants`, `motif`, `idUtilisateur`, `idRessource`) VALUES
                                                                                                                                                              (3, '2025-05-20', '22:00', '22:20', 'Terminé', 14, 'gdfsg', 3, 4),
                                                                                                                                                              (4, '2025-05-20', '22:00', '22:20', 'Confirmed', 12, 'aziz', 3, 5);

-- --------------------------------------------------------

--
-- Table structure for table `ressources`
--

DROP TABLE IF EXISTS `ressources`;
CREATE TABLE IF NOT EXISTS `ressources` (
                                            `id` int NOT NULL AUTO_INCREMENT,
                                            `nom` varchar(255) COLLATE utf8mb4_general_ci NOT NULL,
    `categorie` varchar(100) COLLATE utf8mb4_general_ci NOT NULL,
    `capacite` int NOT NULL,
    `tarif_horaire` double NOT NULL,
    `horaire_ouverture` varchar(50) COLLATE utf8mb4_general_ci NOT NULL,
    `horaire_fermeture` varchar(50) COLLATE utf8mb4_general_ci NOT NULL,
    `description` text COLLATE utf8mb4_general_ci,
    `disponible` tinyint(1) DEFAULT '1',
    PRIMARY KEY (`id`),
    KEY `idx_categorie` (`categorie`),
    KEY `idx_disponible` (`disponible`)
    ) ENGINE=InnoDB AUTO_INCREMENT=18 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `ressources`
--

INSERT INTO `ressources` (`id`, `nom`, `categorie`, `capacite`, `tarif_horaire`, `horaire_ouverture`, `horaire_fermeture`, `description`, `disponible`) VALUES
                                                                                                                                                            (1, 'salle', 'Salle de réunion', 10, 15, '20:00', '22:00', 'dc,qs;,c', 1),
                                                                                                                                                            (2, 'salle', 'Salle de conférence', 15, 15, '20:00', '22:00', 'xjhjnknlq', 1),
                                                                                                                                                            (3, 'voiture ', 'vehicule', 4, 120, '08:00', '18:00', 'bdjkskjl', 1),
                                                                                                                                                            (4, 'bmw', 'Véhicule', 4, 150, '11:00', '18:00', 'xvhgqskj', 1),
                                                                                                                                                            (6, 'chaise', 'Équipement', 1, 10, '08:00', '18:00', 'dkjql', 1),
                                                                                                                                                            (7, 'chaises', 'Équipement', 1, 10, '08:00', '22:00', 'shjbkhk', 1),
                                                                                                                                                            (8, 'hhhh', 'Équipement', 55, 25, '87', '45', 'lol', 1),
                                                                                                                                                            (9, 'kjj', 'Salle de réunion', 25, 88, '08:55', '18:00', 'khhgjkll', 1),
                                                                                                                                                            (10, 'll', 'Espace public', 55, 88, '45', '55', 'mm', 1),
                                                                                                                                                            (11, 'bmw', 'Véhicule', 4, 130, '80', '18', 'dfgg', 1),
                                                                                                                                                            (12, 'Ressource', 'Équipement', 100, 20, '08:00', '18:00', 'Ceci est une description', 1),
                                                                                                                                                            (13, 'Ademressource', 'Véhicule', 40, 12, '08:00', '18:00', 'Ceci est une description d\'une véhicule', 1),
(14, 'Une autre ressource', 'Espace public', 100, 14, '09:00', '20:00', 'validation', 1),
(15, 'Volkswagen Golf', 'Véhicule', 5, 10, '08:00', '18:00', 'T5UNIJu', 1),
(16, 'kikk', 'Véhicule', 12, 55, '25:22', '45:55', 'jooo', 1),
(17, 'oooo', 'Véhicule', 55, 12, '12;00', '112::44', 'kpiuiop', 1);

-- --------------------------------------------------------

--
-- Table structure for table `taches`
--

DROP TABLE IF EXISTS `taches`;
CREATE TABLE IF NOT EXISTS `taches` (
  `id` int NOT NULL AUTO_INCREMENT,
  `nom` varchar(255) NOT NULL,
  `categorie` varchar(50) NOT NULL,
  `projet_id` int NOT NULL,
  PRIMARY KEY (`id`),
  KEY `projet_id` (`projet_id`)
) ENGINE=MyISAM AUTO_INCREMENT=21 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

--
-- Dumping data for table `taches`
--

INSERT INTO `taches` (`id`, `nom`, `categorie`, `projet_id`) VALUES
(1, 'Concevoir l\'interface', 'À faire', 1),
(2, 'Réaliser le prototypage', 'À faire', 1),
(3, 'Développer l\'authentification', 'En cours', 1),
(4, 'Tester sur mobile', 'En cours', 1),
(5, 'Réunion de lancement', 'Terminé', 1),
(6, 'Validation des maquettes', 'Terminé', 1),
(7, 'acheter des billets d\'avion pour les client', 'À faire', 2),
(8, 'c\'est 00h , il faut que je dore', 'À faire', 4),
(9, 'preparer dejouner', 'En cours', 4),
(10, 'dore', 'En cours', 5),
(12, 'dore', 'Terminé', 6),
(13, 'drive', 'Terminé', 8),
(14, 'etudier', 'Terminé', 9),
(15, 'achref', 'Terminé', 10),
(16, 'etudiante', 'En cours', 11),
(17, 'IT', 'Terminé', 12),
(18, 'manger', 'Terminé', 13),
(19, 'etudier', 'En cours', 14),
(20, 'etudier', 'En cours', 15);

-- --------------------------------------------------------

--
-- Table structure for table `utilisateur`
--

DROP TABLE IF EXISTS `utilisateur`;
CREATE TABLE IF NOT EXISTS `utilisateur` (
  `id_user` int NOT NULL AUTO_INCREMENT,
  `nom_user` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `prenom_user` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `email` varchar(100) NOT NULL,
  `motDePasse` varchar(100) NOT NULL,
  `role` enum('CITOYEN','EMPLOYE','ADMIN') NOT NULL DEFAULT 'CITOYEN',
  `actif` tinyint(1) NOT NULL DEFAULT '1',
  `cin` varchar(8) DEFAULT NULL,
  `adresse` varchar(255) DEFAULT NULL,
  `telephone` varchar(20) DEFAULT NULL,
  `matricule` varchar(20) DEFAULT NULL,
  `poste` varchar(100) DEFAULT NULL,
  `date_embauche` date DEFAULT NULL,
  `departement` varchar(100) DEFAULT NULL,
  `date_creation` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id_user`),
  UNIQUE KEY `email` (`email`)
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

--
-- Dumping data for table `utilisateur`
--

INSERT INTO `utilisateur` (`id_user`, `nom_user`, `prenom_user`, `email`, `motDePasse`, `role`, `actif`, `cin`, `adresse`, `telephone`, `matricule`, `poste`, `date_embauche`, `departement`, `date_creation`) VALUES
(2, 'Admin', 'Système', 'admin@mairie.tn', 'admin123', 'ADMIN', 1, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2025-05-08 01:47:34'),
(3, 'oussema', 'neffati', 'oussema@esprit.tn', 'oussNEFF98', 'CITOYEN', 1, '12345678', 'menzel jemil', '23456789', NULL, NULL, NULL, NULL, '2025-05-08 02:38:39'),
(4, 'roua', 'ghaffari', 'roua@gmail.com', 'Roua123@', 'CITOYEN', 1, '13290954', 'ariana', '29875583', NULL, NULL, NULL, NULL, '2025-05-11 12:53:04'),
(5, 'Emna', 'ben', 'emna@gmail.com', 'Emna12345', 'CITOYEN', 1, '13290987', 'ariana', '54314811', NULL, NULL, NULL, NULL, '2025-05-11 12:54:50');
COMMIT;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
