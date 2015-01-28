app = angular.module("app", ['ui.bootstrap'])

app.controller "controlCard", ($scope, $http) ->
    $('.flip').click ->
        $(this).find('.card').toggleClass('flipped')

    $scope.cards = []

    $scope.card = {word: null, read: null, mean: null}

    $scope.getCards = () =>
        $http.get("/cards")
        .success (data, status, headers, config) ->
            $scope.cards = data
        .error (data, status, headers, config) ->
            $scope.cards = []
            $scope.alerts.push {type: 'danger', msg: 'Loaded Error'}

    $scope.getCards()

    $scope.currentIndex = 0
    $scope.currentFlip = false

    $scope.isCurrentSlideIndex = (index) ->
        $scope.currentIndex is index

    $scope.next = () ->
        $scope.currentFlip = false
        $scope.currentIndex = if ($scope.currentIndex < $scope.cards.length - 1) then ++$scope.currentIndex else 0

    $scope.prev = () ->
        $scope.currentFlip = false
        $scope.currentIndex = if ($scope.currentIndex > 0) then --$scope.currentIndex else $scope.cards.length - 1

    $scope.flip = () ->
        $scope.currentFlip = !$scope.currentFlip;

    $scope.addCard = () ->
        card = {word: $scope.card.word, read: $scope.card.read, mean: $scope.card.mean}
        $http.post("/card", card)
        .success () ->
            $scope.cards.push(card)
            $scope.card.word = null
            $scope.card.read = null
            $scope.card.mean = null
            $scope.currentIndex = $scope.cards.length - 1
        .error () ->
            $scope.cards.pop()
            $scope.alerts.push {type: 'danger', msg: 'Error'}

    $scope.alerts = []

    $scope.closeAlert = (index) ->
        $scope.alerts.splice(index, 1)

app.controller "controlLayout", ($scope) ->
    $scope.menus = [
        {
            text: "Home"
            url: "/"
        }
        {
            text: "Flash Card"
            url: "/flashcard"
        }
        {
            text: "Chat Room"
            url: "/chat"
        }
#        {
#            text: "Test something fun"
#            url: "/test"
#        }
    ]

    $scope.active = (index, menuIndex) ->
        if menuIndex is index then "active" else ""

app.controller "controlChat", ($scope) ->
    $scope.messages = []
    $scope.socket = null

    $scope.$watch '$scope.username', () ->
        $scope.join ($scope.username)

    $scope.join = (username) ->
        if $scope.socket then $scope.socket.close
        socket = new WebSocket("ws://" + window.location.host + "/sendChat/" + username)
        socket.open = () ->
        socket.onclose = () -> console.log "User's leaved!"
        socket.onmessage = (e) ->
            $scope.$apply () -> $scope.messages.push JSON.parse e.data

        $scope.socket = socket;

    $scope.send = (message) ->
        $scope.socket.send(message)
        $scope.message = ""

app.controller "controlIndex", ($scope, $http) ->
    $scope.signIn = () ->
        user = {username: $scope.user.username, password: $scope.user.password}
        $http.post("/login", user)
        .success () ->
            $scope.username = $scope.user.username
            $scope.loggedIn = true
        .error () ->
            alert("Log in Failed")
            $scope.loggedIn = false

    $scope.signOut = () ->
        $scope.loggedIn = false
        $http.get("/logout")
