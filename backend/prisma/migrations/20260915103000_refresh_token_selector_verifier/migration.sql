-- RefreshToken table was empty (dev data only), so this is a plain
-- destructive column swap rather than a data migration.
ALTER TABLE "RefreshToken" DROP COLUMN "token";
ALTER TABLE "RefreshToken" ADD COLUMN "selector" TEXT NOT NULL;
ALTER TABLE "RefreshToken" ADD COLUMN "verifierHash" TEXT NOT NULL;

CREATE UNIQUE INDEX "RefreshToken_selector_key" ON "RefreshToken"("selector");
