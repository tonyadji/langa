import React, { useState, useEffect } from 'react';
import { RetentionUnit } from '@/types';
import type { RetentionPolicy } from '@/types';
import { Button } from '@/components/common/Button';
import { Input } from '@/components/common/Input';
import { Clock, Check } from 'lucide-react';

interface RetentionPolicySettingsProps {
  initialPolicy?: RetentionPolicy;
  onSave: (duration: number, unit: RetentionUnit) => Promise<void>;
  isLoading?: boolean;
}

export const RetentionPolicySettings: React.FC<RetentionPolicySettingsProps> = ({
  initialPolicy,
  onSave,
  isLoading = false,
}) => {
  const [duration, setDuration] = useState(initialPolicy?.duration || 30);
  const [unit, setUnit] = useState<RetentionUnit>(initialPolicy?.unit || RetentionUnit.Days);
  const [showSuccess, setShowSuccess] = useState(false);
  
  // Update local state when initialPolicy changes (e.g. after fetch)
  useEffect(() => {
    if (initialPolicy) {
        setDuration(initialPolicy.duration);
        setUnit(initialPolicy.unit);
    }
  }, [initialPolicy]);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    try {
      await onSave(duration, unit);
      setShowSuccess(true);
      setTimeout(() => setShowSuccess(false), 3000);
    } catch (e) {
      // Error handled by parent
    }
  };

  const units = [RetentionUnit.Days];

  return (
    <div className="bg-white dark:bg-gray-800 border border-gray-200 dark:border-gray-700 rounded-lg p-6">
      <div className="flex items-center gap-2 mb-4">
        <Clock className="text-blue-600 dark:text-blue-400" size={20} />
        <h3 className="text-lg font-semibold text-gray-900 dark:text-gray-100">
          Data Retention Policy
        </h3>
      </div>
      
      <p className="text-sm text-gray-600 dark:text-gray-400 mb-6">
        Configure how long logs and metrics are stored (1-45 days). Older data will be automatically deleted.
      </p>

      <form onSubmit={handleSubmit} className="flex flex-col sm:flex-row gap-4 items-end">
        <div className="flex-1 w-full sm:w-auto">
          <Input
            label="Duration"
            type="number"
            min={1}
            max={45}
            value={duration}
            onChange={(e) => setDuration(parseInt(e.target.value) || 0)}
            required
            aria-label="Retention duration"
          />
        </div>
        
        <div className="flex-1 w-full sm:w-auto">
          <label className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">
            Unit
          </label>
          <select
            value={unit}
            onChange={(e) => setUnit(e.target.value as RetentionUnit)}
            className="w-full h-[42px] px-3 py-2 bg-white dark:bg-gray-700 border border-gray-300 dark:border-gray-600 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent outline-none transition-all text-gray-900 dark:text-gray-100"
            disabled={isLoading}
            aria-label="Retention unit"
          >
            {units.map((u) => (
              <option key={u} value={u}>
                {u}
              </option>
            ))}
          </select>
        </div>

        <div className="w-full sm:w-auto flex items-center gap-2">
            {showSuccess && (
              <span className="text-green-600 dark:text-green-400 flex items-center gap-1 text-sm font-medium animate-in fade-in duration-300">
                <Check size={16} />
                Saved
              </span>
            )}
            <Button type="submit" isLoading={isLoading} className="w-full sm:w-auto h-[42px]">
                Save Changes
            </Button>
        </div>
      </form>
    </div>
  );
};
