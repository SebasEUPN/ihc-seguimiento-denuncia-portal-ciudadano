/* =====================================================================
   AMPLIACIÓN DE BD_Denuncias PARA EL PORTAL CIUDADANO (Prototipo 2)

   Requisito: haber ejecutado antes 01_crear_base_datos.sql (primer prototipo).

   Este script SOLO AGREGA columnas e índices. No borra tablas ni datos y
   se puede ejecutar varias veces sin problema (cada paso comprueba antes
   si ya fue aplicado). El primer prototipo sigue funcionando igual porque
   las columnas nuevas son opcionales o tienen un valor por defecto.

   Cómo usarlo: abrirlo en SQL Server Management Studio y presionar F5.
   ===================================================================== */

USE BD_Denuncias;
GO

/* ---------------------------------------------------------------------
   1. CIUDADANO: PIN de acceso al portal
   --------------------------------------------------------------------- */

-- PIN de 6 dígitos cifrado con BCrypt (nunca se guarda el PIN real)
IF COL_LENGTH('ciudadano', 'pin_hash') IS NULL
    ALTER TABLE ciudadano ADD pin_hash NVARCHAR(100) NULL;
GO

-- Cantidad de intentos fallidos seguidos al escribir el PIN
IF COL_LENGTH('ciudadano', 'pin_intentos') IS NULL
    ALTER TABLE ciudadano ADD pin_intentos INT NOT NULL
        CONSTRAINT DF_ciudadano_pin_intentos DEFAULT 0;
GO

-- Si tiene fecha, la consulta está bloqueada hasta ese momento (demasiados intentos)
IF COL_LENGTH('ciudadano', 'pin_bloqueado_hasta') IS NULL
    ALTER TABLE ciudadano ADD pin_bloqueado_hasta DATETIME2 NULL;
GO

/* ---------------------------------------------------------------------
   2. DENUNCIA: funcionario policial responsable
      Es el funcionario que el ciudadano ve en el portal y con quien
      conversa por el chat. NULL = todavía sin asignar.
   --------------------------------------------------------------------- */
IF COL_LENGTH('denuncia', 'id_usuario_responsable') IS NULL
    ALTER TABLE denuncia ADD id_usuario_responsable BIGINT NULL;
GO

-- Clave foránea: el responsable debe existir en la tabla usuario
IF NOT EXISTS (SELECT 1 FROM sys.foreign_keys WHERE name = N'FK_denuncia_responsable')
    ALTER TABLE denuncia ADD CONSTRAINT FK_denuncia_responsable
        FOREIGN KEY (id_usuario_responsable) REFERENCES usuario (id_usuario);
GO

/* ---------------------------------------------------------------------
   3. ÍNDICES para las búsquedas del portal
   --------------------------------------------------------------------- */

-- "Mis denuncias": todas las denuncias de un ciudadano
IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = N'IX_denuncia_ciudadano')
    CREATE INDEX IX_denuncia_ciudadano ON denuncia (id_ciudadano);
GO

-- Chat: conversación de un ciudadano sobre una denuncia
IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = N'IX_conversacion_ciudadano')
    CREATE INDEX IX_conversacion_ciudadano ON conversacion (id_ciudadano, referencia);
GO

PRINT N'Ampliación aplicada. Ahora ejecuta la aplicación portal-ciudadano: al arrancar asigna el PIN de demostración y los funcionarios responsables.';
GO
