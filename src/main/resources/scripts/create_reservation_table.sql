-- Drop existing table if it exists
DROP TABLE IF EXISTS `reservation`;

-- Create reservation table with correct structure
CREATE TABLE `reservation` (
  `id_res` INT AUTO_INCREMENT PRIMARY KEY,
  `date_reservation` DATE NOT NULL,
  `heure_debut` VARCHAR(10) NOT NULL,
  `heure_fin` VARCHAR(10) NOT NULL,
  `status` VARCHAR(50) NOT NULL,
  `nombre_participants` INT NOT NULL,
  `motif` TEXT NOT NULL,
  `id_utilisateur` INT NOT NULL,
  `id_ressource` INT NOT NULL,
  -- Add foreign key constraints if needed
  -- FOREIGN KEY (`id_utilisateur`) REFERENCES `utilisateur`(`id`),
  -- FOREIGN KEY (`id_ressource`) REFERENCES `ressource`(`id`),
  INDEX `idx_utilisateur` (`id_utilisateur`),
  INDEX `idx_ressource` (`id_ressource`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- Add sample data if needed for testing
-- INSERT INTO `reservation` (`date_reservation`, `heure_debut`, `heure_fin`, `status`, `nombre_participants`, `motif`, `id_utilisateur`, `id_ressource`) 
-- VALUES 
-- (CURDATE(), '09:00', '10:00', 'CONFIRMEE', 5, 'Réunion de test', 1, 1);

