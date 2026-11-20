package com.connectx.app.domain.model

class UsernameTakenException : Exception("username_taken")

class UsernameReservedException : Exception("username_reserved")
