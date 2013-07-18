/*
 *  Copyright 2009 David Johnson, School of Biological Sciences,
 *  University of Reading, UK.
 *
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package uk.ac.rdg.evoportal.wizards;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.extensions.ajax.markup.html.autocomplete.AutoCompleteTextField;
import org.apache.wicket.extensions.wizard.Wizard;
import org.apache.wicket.extensions.wizard.WizardModel;
import org.apache.wicket.extensions.wizard.WizardModel.ICondition;
import org.apache.wicket.extensions.wizard.WizardStep;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.CheckBox;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.TextArea;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.util.string.Strings;
import org.apache.wicket.validation.validator.MinimumValidator;
import org.apache.wicket.validation.validator.RangeValidator;
import org.biojava.bio.seq.io.ParseException;
import uk.ac.rdg.evoportal.GlobalConstants;
import uk.ac.rdg.evoportal.LoginSession;
import uk.ac.rdg.evoportal.data.InputFilesDataProvider;
import uk.ac.rdg.evoportal.pages.ComputeJobs;
import uk.ac.rdg.evoportal.tasks.ComputeJobSubmitTask;
import uk.ac.rdg.util.NexusUtil;
/**
 *
 * @author david
 */
public class ComputeJobWizard extends Wizard {
    
    private BPAnalysisSettings analysis = new BPAnalysisSettings();
    private BPJobSettings job = new BPJobSettings();
    IModel<BPJobSettings> jobModel = new Model<BPJobSettings>(job);
    transient Logger LOG = Logger.getLogger(ComputeJobWizard.class.getName());

    // generates a List with a sequence of ints
    private List getIntSeqList(int from, int to) {
        List l = new Vector<Integer>();
        for (int i=from;i<to+1;i++) {
            l.add(i);
        }
        return l;
    }

    private final class ModelStep extends WizardStep {

        IModel<BPAnalysisSettings> analysisModel = new Model<BPAnalysisSettings>(analysis);

        public ModelStep() {
            super();
            setTitleModel(new ResourceModel("modelstep.title"));
            setSummaryModel(new StringResourceModel("modelstep.summary", analysisModel));

            List<String> filesList = new InputFilesDataProvider(((LoginSession)getSession()).getUsername()).listFiles();
            DropDownChoice filesDropDown = new DropDownChoice("analysis.fileName", filesList);
            filesDropDown.setRequired(true);

            List<BPModel> modelChoices = Arrays.asList(BPModel.values());
            List<BPBaseFrequencyType> baseFreqChoices = Arrays.asList(BPBaseFrequencyType.values());
            List<BPRateVariationType> rateVariationChoices = Arrays.asList(BPRateVariationType.values());

            DropDownChoice modelsDropDown = new DropDownChoice("analysis.model", modelChoices);
            DropDownChoice baseFreqDropDown = new DropDownChoice("analysis.baseFrequencies", baseFreqChoices);
            DropDownChoice rateVariationDropDown = new DropDownChoice("analysis.rateVariation", rateVariationChoices);
            final DropDownChoice categoriesRateVarDropDown = new DropDownChoice("analysis.rateVariationCategories", getIntSeqList(2, 12));
            final DropDownChoice gtrPatternIntsDropDown = new DropDownChoice("analysis.patterns", getIntSeqList(1, 20));
            final CheckBox gtrPatternsRJ = new CheckBox("analysis.patternsReversibleJump");

            final DropDownChoice branchLengthValueDropDown = new DropDownChoice("analysis.branchLengthSets", getIntSeqList(1, 5));
            CheckBox gtrBranchSetsRJ = new CheckBox("analysis.branchLengthSetsReversibleJump");
            DropDownChoice topologiesDropDown = new DropDownChoice("analysis.topologies", getIntSeqList(1, 5));

            final DropDownChoice covarionDropDown = new DropDownChoice("analysis.covarionValue", getIntSeqList(2, 6));
            covarionDropDown.setEnabled(false);
            covarionDropDown.setOutputMarkupId(true);
            CheckBox covarionOn = new CheckBox("analysis.covarionOn");
            covarionOn.add(new AjaxFormComponentUpdatingBehavior("onchange") {
                @Override
                protected void onUpdate(AjaxRequestTarget target) {
                    if (analysis.isCovarionOn()) {
                        target.addComponent(covarionDropDown.setEnabled(true));
                    } else {
                        target.addComponent(covarionDropDown.setEnabled(false));
                    }
                }
            });

            CheckBox invariantSites = new CheckBox("analysis.invariantSitesOn");
            CheckBox coolingOn = new CheckBox("analysis.coolingOn");

            gtrPatternIntsDropDown.setOutputMarkupId(true);
            gtrPatternsRJ.setOutputMarkupId(true);
            modelsDropDown.add(new AjaxFormComponentUpdatingBehavior("onchange") {
                @Override
                protected void onUpdate(AjaxRequestTarget target) {
                    if (BPModel.GTR.equals(analysis.getModel())) {
                        target.addComponent(gtrPatternIntsDropDown.setEnabled(false));
                        target.addComponent(gtrPatternsRJ.setEnabled(true));
                        analysis.setPatternsReversibleJump(true);
                    } else {
                        target.addComponent(gtrPatternIntsDropDown.setEnabled(false));
                        target.addComponent(gtrPatternsRJ.setEnabled(false));
                        analysis.setPatternsReversibleJump(false);
                    }
                }
            });

            gtrPatternsRJ.add(new AjaxFormComponentUpdatingBehavior("onchange") {
                @Override
                protected void onUpdate(AjaxRequestTarget target) {
                    if (analysis.isPatternsReversibleJump()) {
                        target.addComponent(gtrPatternIntsDropDown.setEnabled(false));
                    } else {
                        target.addComponent(gtrPatternIntsDropDown.setChoices(getIntSeqList(1, 20)).setEnabled(true));
                        analysis.setPatterns(1);
                    }
                }
            });

            branchLengthValueDropDown.setOutputMarkupId(true);
            gtrBranchSetsRJ.add(new AjaxFormComponentUpdatingBehavior("onchange") {
                @Override
                protected void onUpdate(AjaxRequestTarget target) {
                    if (analysis.isBranchLengthSetsReversibleJump()) {
                        if (analysis.getBranchLengthSets()<2) {
                            analysis.setBranchLengthSets(2);
                        }
                        target.addComponent(branchLengthValueDropDown.setChoices(getIntSeqList(2, 5)));
                    } else {
                        target.addComponent(branchLengthValueDropDown.setChoices(getIntSeqList(1, 5)));
                    }
                }
            });

            categoriesRateVarDropDown.setOutputMarkupId(true);
            rateVariationDropDown.add(new AjaxFormComponentUpdatingBehavior("onchange") {
                @Override
                protected void onUpdate(AjaxRequestTarget target) {
                    if (BPRateVariationType.Gamma.equals(analysis.getRateVariation())) {
                        target.addComponent(categoriesRateVarDropDown
                                .setChoices(getIntSeqList(2, 12)).setEnabled(true));
                        analysis.setRateVariationCategories(4);
                    } else {
                        target.addComponent(categoriesRateVarDropDown.setEnabled(false));
                    }
                }
            });

            final TextField coPosText = new TextField("analysis.coolingPos");
            coPosText.setEnabled(false);
            final TextField coNegText = new TextField("analysis.coolingNeg");
            coNegText.setEnabled(false);

            coPosText.setOutputMarkupId(true);
            coNegText.setOutputMarkupId(true);
            coolingOn.add(new AjaxFormComponentUpdatingBehavior("onchange") {
                @Override
                protected void onUpdate(AjaxRequestTarget target) {
                    if (analysis.isCoolingOn()) {
                        target.addComponent(coPosText.setEnabled(true));
                        target.addComponent(coNegText.setEnabled(true));
                    } else {
                        target.addComponent(coPosText.setEnabled(false));
                        target.addComponent(coNegText.setEnabled(false));
                    }
                }
            });
            add(filesDropDown);
            add(modelsDropDown);
            add(baseFreqDropDown);
            add(rateVariationDropDown);
            add(categoriesRateVarDropDown);
            add(gtrPatternIntsDropDown);
            add(gtrPatternsRJ);
            add(branchLengthValueDropDown);
            add(gtrBranchSetsRJ);
            add(topologiesDropDown);

            add(covarionDropDown);
            add(covarionOn);
            add(invariantSites);

            add(coolingOn);
            // following 2 fields only required if coolingOn
            add(coPosText
                    .setType(Integer.class)
                    .add(new RangeValidator(1000, 10000000)));
            add(coNegText
                    .setType(Double.class)
                    .add(new RangeValidator(-1000.0, 0.0)));


            TextArea rtText = new TextArea("analysis.root");
            TextField ogTextField = new TextField("analysis.outGroup");
            add(ogTextField);
            add(rtText);
            
            add(new TextField("analysis.seed")
                    .setRequired(true)
                    .setType(Integer.class)
                    .add(new MinimumValidator(0)));
            add(new TextArea("analysis.other"));

            if (BPModel.GTR.equals(analysis.getModel())) {
                if (analysis.isPatternsReversibleJump()) {
                    gtrPatternIntsDropDown.setEnabled(false);
                    analysis.setPatternsReversibleJump(true);
                } else {
                    gtrPatternIntsDropDown.setEnabled(true);
                    analysis.setPatterns(1);
                    analysis.setPatternsReversibleJump(false);
                }
            } else {
                analysis.setPatternsReversibleJump(false);
                gtrPatternIntsDropDown.setEnabled(false);
                gtrPatternsRJ.setEnabled(false);
            }
            if (!analysis.isCovarionOn()) {
                covarionDropDown.setEnabled(false);
            }
        }

//        /*
//         * TODO: Fix this... getMatrixLabels strips punctuation
//         */
//        private List getOgChoices() {
//            List matrixLabels = null;
//            List ogChoices = new Vector();
//            ogChoices.add("");
//            String fileName = analysis.getFileName();
//            File f = new File (GlobalConstants.getProperty("local.fileroot") + "/" + ((LoginSession)getSession()).getUser() + "/mynexusfiles/", fileName);
//            try {
//                matrixLabels = NexusUtil.getMatrixLabels(f);
//                ogChoices.addAll(matrixLabels);
//            } catch (IOException ex) {
//                Logger.getLogger(ModelStep.class.getName()).log(Level.SEVERE, null, ex);
//            } catch (ParseException ex) {
//                Logger.getLogger(ModelStep.class.getName()).log(Level.SEVERE, null, ex);
//            }
//            return ogChoices;
//        }
    }

    private final class JobStep extends WizardStep {

        IModel<BPAnalysisSettings> analysisModel = new Model<BPAnalysisSettings>(analysis);
        List<Integer> printFrequencyChoices = Arrays.asList(new Integer[]{
           1000, 2500, 5000, 7500, 10000, 12500, 15000, 17500,
           20000, 22500, 25000});
        List<BPRunDurationType> durationTypeChoices = Arrays.asList(BPRunDurationType.values());

        public JobStep() {
            super();
            setTitleModel(new ResourceModel("jobstep.title"));
            setSummaryModel(new StringResourceModel("jobstep.summary", analysisModel));

            DropDownChoice runsDropDown = new DropDownChoice("job.runs", getIntSeqList(1, 20));
            DropDownChoice nodesDropDown = new DropDownChoice("job.nodes", getIntSeqList(1, 50));
            DropDownChoice ppnDropDown = new DropDownChoice("job.ppn", Arrays.asList(new Integer[]{2, 4}));

            final TextField durationTextField = new TextField("job.hours", Integer.class);
            final TextField iterationsTextField = new TextField("analysis.iterations", Integer.class);
            iterationsTextField.setOutputMarkupId(true);
            
            CheckBox infiniteCheckBox = new CheckBox("job.infinite");
            infiniteCheckBox.add(new AjaxFormComponentUpdatingBehavior("onchange") {
                @Override
                protected void onUpdate(AjaxRequestTarget target) {
                    if (job.isInfinite()) {
                        analysis.setIterations(-1);
                        target.addComponent(iterationsTextField.setEnabled(false));
                    } else {
                        target.addComponent(iterationsTextField.setEnabled(true));
                        analysis.setIterations(10000);
                    }
                }
            });

            AutoCompleteTextField pfTextField = new AutoCompleteTextField("analysis.printFrequency") {
                @Override
                protected Iterator getChoices(String input) {
                    if (Strings.isEmpty(input)) {
                        List<String> emptyList = Collections.emptyList();
                        return emptyList.iterator();
                    }

                    List<String> choices = new ArrayList<String>(10);

                    for (final Integer pf : printFrequencyChoices)
                    {
                        if (pf.toString().startsWith(input.toUpperCase()))
                        {
                            choices.add(String.valueOf(pf));
                            if (choices.size() == 10)
                            {
                                break;
                            }
                        }
                    }

                    return choices.iterator();

                }
            };
            durationTextField.setOutputMarkupId(true);      

            add(new TextField("job.name")
                    .setType(String.class)
                    .setRequired(true));
            add(iterationsTextField
                    .setRequired(true)
                    .add(new MinimumValidator(1)));
            add(infiniteCheckBox);
            add(durationTextField
                    .setRequired(true)
                    .add(new MinimumValidator(-1)));
            add(runsDropDown);
            add(nodesDropDown);
            add(ppnDropDown);
            add(pfTextField
                    .setType(Integer.class)
                    .add(new MinimumValidator(1000)));
        }

    }

    private class ConfirmationStep extends WizardStep implements ICondition {

        IModel<BPAnalysisSettings> analysisModel = new Model<BPAnalysisSettings>(analysis);

        public ConfirmationStep() {
            super();
            setTitleModel(new ResourceModel("confirmationstep.title"));
            setSummaryModel(new StringResourceModel("confirmationstep.summary", this, analysisModel));
            add(new Label("job.name"));
            add(new Label("job.runs"));
            add(new Label("job.nodes"));
            add(new Label("job.ppn"));
            add(new Label("job.hours"));
            add(new TextArea("analysis.controlBlock"));  // doesn't update if go back!!!
        }

        public boolean evaluate() {
            return !("".equals(analysis.getFileName()));
        }

    }

    public ComputeJobWizard(String id) {
        super(id);
        setDefaultModel(new CompoundPropertyModel<ComputeJobWizard>(this));
        WizardModel model = new WizardModel();
        model.add(new ModelStep());
        model.add(new JobStep());
        model.add(new ConfirmationStep());
        init(model);
    }

    @Override
    public void onCancel() {
        super.onCancel();
        setResponsePage(ComputeJobs.class);
    }

    @Override
    public void onFinish() {
        super.onFinish();
        if (job.getRuns()==1) {
            File nexusFile = new File(GlobalConstants.getProperty("local.fileroot") + "/" + ((LoginSession)getSession()).getUsername() + "/mynexusfiles/", analysis.getFileName());
            try {
                // double check file is clean of 'other' blocks
                NexusUtil.cleanNexusDataFile(nexusFile);

                // copy the original nexus file out to the tmp file
                FileInputStream in = new FileInputStream(nexusFile);
                File tempFile = File.createTempFile(analysis.getFileName(), ".tmp");
                FileOutputStream out = new FileOutputStream(tempFile);
                byte[] buf = new byte[2048];               
                int l = 0;
                while ((l = in.read(buf))>-1) {
                    out.write(buf, 0, l);
                }
                out.close();
                in.close();

                // write BayesPhylogenies block, uses different kind of writer to write from String
                FileWriter writer = new FileWriter(tempFile, true);
                BufferedWriter cout = new BufferedWriter(writer);
                cout.write(analysis.getControlBlock());
                cout.close();
                
                // if all went OK, build new submit task and send off file
                ComputeJobSubmitTask s = new ComputeJobSubmitTask(
                        ((LoginSession)getSession()).getUsername(),
                        job.getName(),
                        job.getHours(),
                        job.getNodes(),
                        job.getPpn(),
                        tempFile,
                        analysis.getFileName()
                    );
                s.run();
                if (!tempFile.delete()) {
                    throw new IOException("There was a problem deleting the temp file: " + tempFile.getCanonicalPath());
                }
            } catch (ParseException ex) {
                LOG.log(Level.SEVERE, null, ex);
            } catch (IOException ex) {
                LOG.log(Level.SEVERE, null, ex);
            }

        } else
        if (job.getRuns()>1) {
            File nexusFile = new File(GlobalConstants.getProperty("local.fileroot") + "/" + ((LoginSession)getSession()).getUsername() + "/mynexusfiles/", analysis.getFileName());
            try {
                // double check file is clean of 'other' blocks
                NexusUtil.cleanNexusDataFile(nexusFile);
                for (int i=1;i<job.getRuns()+1;i++) {
                    // copy the original nexus file out to the tmp file
                    FileInputStream in = new FileInputStream(nexusFile);
                    File tempFile = File.createTempFile(analysis.getFileName(), ".tmp");
                    FileOutputStream out = new FileOutputStream(tempFile);
                    byte[] buf = new byte[2048];
                    int l = 0;
                    while ((l = in.read(buf))>-1) {
                        out.write(buf, 0, l);
                    }
                    out.close();
                    in.close();

                    // write BayesPhylogenies block, uses different kind of writer to write from String
                    FileWriter writer = new FileWriter(tempFile, true);
                    BufferedWriter cout = new BufferedWriter(writer);
                    analysis.setSeed(new Random().nextInt(Integer.MAX_VALUE)); // needs to create a new random seed for each run
                    cout.write(analysis.getControlBlock());
                    cout.close();

                    // if all went OK, build new submit task and send off file
                    ComputeJobSubmitTask s = new ComputeJobSubmitTask(
                            ((LoginSession)getSession()).getUsername(),
                            job.getName() + "_run-" + i,
                            job.getHours(),
                            job.getNodes(),
                            job.getPpn(),
                            tempFile,
                            analysis.getFileName()
                        );
                    s.run();
                    if (!tempFile.delete()) {
                        throw new IOException("There was a problem deleting the temp file: " + tempFile.getCanonicalPath());
                    }
                }
            } catch (IOException ex) {
                LOG.log(Level.SEVERE, null, ex);
            } catch (ParseException ex) {
                LOG.log(Level.SEVERE, null, ex);
            }
        }
        setResponsePage(ComputeJobs.class);
    }
}
