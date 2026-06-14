-- CreateTable
CREATE TABLE `Demand` (
    `id` INTEGER NOT NULL AUTO_INCREMENT,
    `parentId` INTEGER NOT NULL,
    `subject` VARCHAR(191) NOT NULL,
    `studentGrade` VARCHAR(191) NOT NULL,
    `timeStartAt` DATETIME(3) NOT NULL,
    `timeEndAt` DATETIME(3) NOT NULL,
    `teacherGenderPreference` VARCHAR(191) NULL,
    `minPrice` DOUBLE NOT NULL,
    `maxPrice` DOUBLE NOT NULL,
    `status` ENUM('OPEN', 'CLAIMED', 'CLOSED') NOT NULL DEFAULT 'OPEN',
    `claimedTeacherId` INTEGER NULL,
    `createdAt` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    `updatedAt` DATETIME(3) NOT NULL,

    INDEX `Demand_status_createdAt_idx`(`status`, `createdAt`),
    INDEX `Demand_parentId_createdAt_idx`(`parentId`, `createdAt`),
    INDEX `Demand_claimedTeacherId_createdAt_idx`(`claimedTeacherId`, `createdAt`),
    PRIMARY KEY (`id`)
) DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- AddForeignKey
ALTER TABLE `Demand` ADD CONSTRAINT `Demand_parentId_fkey` FOREIGN KEY (`parentId`) REFERENCES `User`(`id`) ON DELETE RESTRICT ON UPDATE CASCADE;

-- AddForeignKey
ALTER TABLE `Demand` ADD CONSTRAINT `Demand_claimedTeacherId_fkey` FOREIGN KEY (`claimedTeacherId`) REFERENCES `User`(`id`) ON DELETE RESTRICT ON UPDATE CASCADE;
