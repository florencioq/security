enablePlugins(FlywayPlugin)

flywayDriver := "org.postgresql.Driver"
flywayUrl := "jdbc:postgresql://localhost:5432/security"
flywayUser := "postgres"
flywayPassword := "postgres"
flywayLocations += "migration"