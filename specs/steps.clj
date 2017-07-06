(require '[jiksnu.helpers.actions :as helpers.action])

(Given #"^a user exists with the password \"(.*?)\"$" [password]
  (helpers.action/register-user password))

(Given #"^I am not logged in$" []
  (comment  Write code here that turns the phrase above into concrete actions  )
  (throw (cucumber.api.PendingException.)))

(Given #"^I am at the \"(.*?)\" page$" [arg1]
  (comment  Write code here that turns the phrase above into concrete actions  )
  (throw (cucumber.api.PendingException.)))

(When #"^I put my username in the \"(.*?)\" field$" [arg1]
  (comment  Write code here that turns the phrase above into concrete actions  )
  (throw (cucumber.api.PendingException.)))

(When #"^I put my password in the \"(.*?)\" field$" [arg1]
  (comment  Write code here that turns the phrase above into concrete actions  )
  (throw (cucumber.api.PendingException.)))

(When #"^I click the \"(.*?)\" link$" [arg1]
  (comment  Write code here that turns the phrase above into concrete actions  )
  (throw (cucumber.api.PendingException.)))

(Then #"^I should be at the \"(.*?)\" page$" [arg1]
  (comment  Write code here that turns the phrase above into concrete actions  )
  (throw (cucumber.api.PendingException.)))

(Then #"^I should be logged in$" []
  (comment  Write code here that turns the phrase above into concrete actions  )
  (throw (cucumber.api.PendingException.)))
