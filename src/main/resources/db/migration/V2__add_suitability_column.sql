-- Add the missing 'suitability' column to the diagnostic_methods table
ALTER TABLE diagnostic_methods 
ADD COLUMN IF NOT EXISTS suitability VARCHAR(20) NOT NULL DEFAULT 'MEDIUM';

-- Update existing rows with appropriate suitability values
-- Methods with HIGH effectiveness and coverage get HIGH suitability
UPDATE diagnostic_methods SET suitability = 'HIGH' 
WHERE id IN (3, 4, 6, 7, 8, 13, 14, 18, 19, 21, 23, 24, 26, 27, 30, 32, 35, 36, 38, 40, 42, 45, 46, 48, 50, 52, 54);

-- Methods with MEDIUM effectiveness or mixed characteristics get MEDIUM suitability
UPDATE diagnostic_methods SET suitability = 'MEDIUM' 
WHERE id IN (1, 2, 5, 9, 10, 12, 15, 17, 20, 22, 25, 28, 29, 31, 33, 34, 37, 39, 41, 43, 44, 47, 49, 51, 53);

-- Methods with LOW coverage or other specific characteristics get LOW suitability
UPDATE diagnostic_methods SET suitability = 'LOW' 
WHERE id IN (11, 16);
