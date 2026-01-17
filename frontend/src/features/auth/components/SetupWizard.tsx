import { useState } from 'react';
import { Button } from '@/components/common/Button';
import { Card } from '@/components/common/Card';

export interface SetupWizardProps {
  onComplete?: () => void;
  onSkip?: () => void;
}

interface Step {
  id: number;
  title: string;
  description: string;
  content: React.ReactNode;
}

export function SetupWizard({ onComplete, onSkip }: SetupWizardProps) {
  const [currentStep, setCurrentStep] = useState(0);
  
  const steps: Step[] = [
    {
      id: 1,
      title: 'Welcome to Langa',
      description: "Let's get you started with monitoring your applications",
      content: (
        <div className="text-center py-8">
          <div className="text-6xl mb-6">🎉</div>
          <h2 className="text-2xl font-bold text-gray-900 mb-4">
            Welcome to Langa!
          </h2>
          <p className="text-gray-600 max-w-md mx-auto">
            Langa helps you monitor your applications with powerful logging and metrics.
            Let's set up your account in just a few steps.
          </p>
        </div>
      ),
    },
    {
      id: 2,
      title: 'Create Your First Application',
      description: 'Applications help you organize your logs and metrics',
      content: (
        <div className="py-8">
          <h2 className="text-2xl font-bold text-gray-900 mb-4">
            What is an Application?
          </h2>
          <p className="text-gray-600 mb-6">
            An application represents a service or project you want to monitor.
            Each application gets unique credentials for sending logs and metrics.
          </p>
          <div className="space-y-4">
            <div className="flex items-start gap-3">
              <span className="text-2xl">📱</span>
              <div>
                <h3 className="font-semibold text-gray-900">Organize by Service</h3>
                <p className="text-sm text-gray-600">
                  Create separate applications for each microservice
                </p>
              </div>
            </div>
            <div className="flex items-start gap-3">
              <span className="text-2xl">🔒</span>
              <div>
                <h3 className="font-semibold text-gray-900">Secure Credentials</h3>
                <p className="text-sm text-gray-600">
                  Each application has unique API keys for ingestion
                </p>
              </div>
            </div>
            <div className="flex items-start gap-3">
              <span className="text-2xl">👥</span>
              <div>
                <h3 className="font-semibold text-gray-900">Share with Teams</h3>
                <p className="text-sm text-gray-600">
                  Invite team members to collaborate on applications
                </p>
              </div>
            </div>
          </div>
        </div>
      ),
    },
    {
      id: 3,
      title: 'You\'re All Set!',
      description: 'Start monitoring your applications now',
      content: (
        <div className="text-center py-8">
          <div className="text-6xl mb-6">✨</div>
          <h2 className="text-2xl font-bold text-gray-900 mb-4">
            You're Ready to Go!
          </h2>
          <p className="text-gray-600 max-w-md mx-auto mb-6">
            You can now create your first application and start sending logs and metrics.
            Check out our documentation to learn more about integration.
          </p>
          <div className="bg-blue-50 border border-blue-200 rounded-lg p-4 text-left">
            <h3 className="font-semibold text-blue-900 mb-2">Quick Tips:</h3>
            <ul className="text-sm text-blue-800 space-y-1">
              <li>• Create an application from the dashboard</li>
              <li>• Copy your API credentials securely</li>
              <li>• Follow integration guides for your framework</li>
              <li>• View real-time logs and metrics</li>
            </ul>
          </div>
        </div>
      ),
    },
  ];
  
  const isFirstStep = currentStep === 0;
  const isLastStep = currentStep === steps.length - 1;
  
  const handleNext = () => {
    if (isLastStep) {
      if (onComplete) {
        onComplete();
      }
    } else {
      setCurrentStep((prev) => prev + 1);
    }
  };
  
  const handleBack = () => {
    setCurrentStep((prev) => Math.max(0, prev - 1));
  };
  
  const handleSkip = () => {
    if (onSkip) {
      onSkip();
    } else if (onComplete) {
      onComplete();
    }
  };
  
  return (
    <Card className="w-full max-w-2xl p-8">
      {/* Progress Indicator */}
      <div className="mb-8">
        <div className="flex justify-between items-center mb-2">
          <span className="text-sm font-medium text-gray-700">
            Step {currentStep + 1} of {steps.length}
          </span>
          {!isLastStep && (
            <button
              onClick={handleSkip}
              className="text-sm text-gray-500 hover:text-gray-700"
            >
              Skip
            </button>
          )}
        </div>
        <div className="h-2 bg-gray-200 rounded-full overflow-hidden">
          <div
            className="h-full bg-blue-600 transition-all duration-300"
            style={{ width: `${((currentStep + 1) / steps.length) * 100}%` }}
          />
        </div>
      </div>
      
      {/* Step Content */}
      <div className="mb-8">
        <div className="mb-6">
          <h1 className="text-sm font-semibold text-blue-600 uppercase tracking-wide mb-1">
            {steps[currentStep].title}
          </h1>
          <p className="text-gray-600">{steps[currentStep].description}</p>
        </div>
        {steps[currentStep].content}
      </div>
      
      {/* Navigation Buttons */}
      <div className="flex justify-between gap-4">
        <Button
          variant="secondary"
          onClick={handleBack}
          disabled={isFirstStep}
        >
          Back
        </Button>
        <Button
          variant="primary"
          onClick={handleNext}
        >
          {isLastStep ? 'Finish' : 'Next'}
        </Button>
      </div>
    </Card>
  );
}
