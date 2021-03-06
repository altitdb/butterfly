package br.edu.utfpr.butterfly.classifier;

import java.util.List;

import br.edu.utfpr.butterfly.model.DevelopmentType;
import br.edu.utfpr.butterfly.model.Episode;
import br.edu.utfpr.butterfly.model.action.Action;

public class TestFirstClassifier extends AbstractClassifier implements Classifier {

	public Episode classify(List<Action> paramActions) {
		List<Action> actions = slimming(paramActions);

		Episode episode = null;
		
		if (actions.size() > 2 && actions.get(0).isTestCreationAction() && actions.get(actions.size() - 1).isTestSuccessfullAction()) {
			actions.remove(0);
			actions.remove(actions.size() - 1);
			
			boolean isTestFirst = true;
			Action isTestOrProduction = actions.get(0);
			if (isTestOrProduction.isTestCodingAction()) {
				for (int i = 0; i < actions.size(); i++) {
					if ((i % 2 == 1 && !actions.get(i).isTestFailureAction()) ||
						(i % 2 == 0 && !actions.get(i).isTestCodingAction())) {
						isTestFirst = false;
						break;
					}
				}
			} else if (isTestOrProduction.isTestFailureAction()) {
				if (actions.size() % 2 == 0) {
					for (int i = 0; i < actions.size(); i++) {
						if ((i % 2 == 1 && !actions.get(i).isProductionCodingAction()) ||
								(i % 2 == 0 && !actions.get(i).isTestFailureAction())) {
							isTestFirst = false;
							break;
						}
					}
				} else {
					while (actions.size() > 1) {
						Action first = actions.get(0);
						Action last = actions.get(actions.size() - 1);
						if (first.getClass().equals(last.getClass())) {
							actions.remove(0);
							actions.remove(actions.size() - 1);
						} else {
							isTestFirst = false;
							break;
						}
					}
				}
			} else {
				isTestFirst = false;
			}
			
			boolean isLastItem = actions.size() == 1;
			if (isLastItem && actions.get(0).isProductionCodingAction()) {
				isTestFirst = true;
			}
			
			if (isTestFirst) {
				episode = createEpisode(paramActions);
			}
		}

		return episode;
	}

	private Episode createEpisode(List<Action> actions) {
		Episode episode = new Episode();
		episode.addActions(actions);
		episode.setClassification(DevelopmentType.TEST_FIRST);
		return episode;
	}

}
