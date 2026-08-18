package io.hxneyw.repo.content.process;

import java.util.UUID;
import org.jetbrains.annotations.NotNull;

public interface IProcessStateProvider {
    @NotNull ProcessState getProcessState();

    @NotNull UUID getProcessIdentity();
}
