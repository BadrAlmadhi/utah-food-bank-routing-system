package com.utahfoodbank.database


import org.ktorm.database.Database

// connect to database
val database = Database.connect(
    url = "jdbc:sqlite:utahfoodbank.db"
)

