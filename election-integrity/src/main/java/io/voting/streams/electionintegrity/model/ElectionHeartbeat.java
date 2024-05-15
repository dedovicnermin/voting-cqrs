package io.voting.streams.electionintegrity.model;

import io.voting.common.library.models.ElectionView;

public record ElectionHeartbeat(String id, String type, ElectionView view) {
}
