import React from 'react';
import { LineChart, Line, XAxis, YAxis, CartesianGrid, Tooltip, Legend, ResponsiveContainer } from 'recharts';
import type { MetricDataPoint, ChartConfig } from '@/types';

interface MetricChartProps {
  data: MetricDataPoint[];
  config: ChartConfig;
  height?: number;
}

export const MetricChart: React.FC<MetricChartProps> = ({ data, config, height = 300 }) => {
  const formatTimestamp = (timestamp: string) => {
    return new Date(timestamp).toLocaleTimeString('en-US', {
      hour: '2-digit',
      minute: '2-digit',
    });
  };

  const formatValue = (value: number) => {
    return `${value.toFixed(2)} ${config.unit}`;
  };

  return (
    <div className="bg-white p-6 rounded-lg shadow">
      <h3 className="text-lg font-semibold text-gray-900 mb-4">{config.title}</h3>
      <ResponsiveContainer width="100%" height={height}>
        <LineChart data={data}>
          <CartesianGrid strokeDasharray="3 3" />
          <XAxis dataKey="timestamp" tickFormatter={formatTimestamp} />
          <YAxis tickFormatter={(value) => `${value}${config.unit}`} />
          <Tooltip
            formatter={(value: number | undefined) => value !== undefined ? formatValue(value) : 'N/A'}
            labelFormatter={(label) => formatTimestamp(label as string)}
          />
          <Legend />
          <Line
            type="monotone"
            dataKey="value"
            name={config.title}
            stroke={config.color}
            strokeWidth={2}
            dot={false}
          />
        </LineChart>
      </ResponsiveContainer>
    </div>
  );
};
