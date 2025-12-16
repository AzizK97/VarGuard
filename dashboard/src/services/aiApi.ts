import axios from 'axios';

const API_URL = 'http://localhost:30082/api/ai';

export interface AiAnalysisResponse {
    alertId: number;
    analysis: string;
    timestamp: string;
}

export interface AiRemediationResponse {
    alertId: number;
    remediation: string;
    timestamp: string;
}

export const aiApi = {
    analyzeAlert: async (alertId: number): Promise<AiAnalysisResponse> => {
        const response = await axios.post(`${API_URL}/analyze/${alertId}`);
        return response.data;
    },

    getRemediation: async (alertId: number): Promise<AiRemediationResponse> => {
        const response = await axios.post(`${API_URL}/remediation/${alertId}`);
        return response.data;
    }
};
