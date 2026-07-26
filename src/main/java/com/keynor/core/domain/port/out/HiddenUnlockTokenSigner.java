package com.keynor.core.domain.port.out;

import com.keynor.core.domain.model.shared.HiddenUnlockToken;

import java.util.Optional;

public interface HiddenUnlockTokenSigner {

    String issue(HiddenUnlockToken token);

    Optional<HiddenUnlockToken> verify(String token);
}
